# Dovewing Car Rentals: Fleet and Rental Tracker (rebuild)

A rebuild of the depot's inherited single-file tool. The features and printed output are the
same as the original, except for the intended differences listed below. The rule the owner says
must never be broken is now enforced: **a vehicle is either available or rented, never both.**

The written diagnosis and the design sketch are in [DIAGNOSIS.md](DIAGNOSIS.md), committed before
the first code change.

## Running it

Plain Java, JDK 21, no dependencies. From the repository root, in PowerShell:

```powershell
Remove-Item -Recurse -Force target/classes -ErrorAction SilentlyContinue
javac -d target/classes (Get-ChildItem -Recurse src/main/java -Filter *.java | ForEach-Object FullName)
java -cp target/classes com.amalitech.dovewing.app.Main
```

On Linux or macOS:

```bash
javac -d target/classes $(find src/main/java -name '*.java')
java -cp target/classes com.amalitech.dovewing.app.Main
```

## Structure

Domain classes are in `src/main/java/com/amalitech/dovewing`. The demo and printing are in a
separate package, `com.amalitech.dovewing.app`, on purpose (see "Encapsulation" below).

| Class                        | Job                                                                                                                                                     |
| ---------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `Depot`                      | The only class that can change rental state. It sees vehicles, renters and rentals together, so it is the only place the rules can be enforced.         |
| `Vehicle`                    | A record. Plate, model, rate, deposit and a `Category`. The compact constructor rejects missing values, a rate of zero or less, and a negative deposit. |
| `Renter`                     | A record. Id, name and a `RenterType` enum, validated in the compact constructor.                                                                       |
| `Rental`                     | Immutable except for its status. The only mutator, `markReturned()`, is package-private, so code outside the package cannot call it.                    |
| `Category`                   | Interface holding the pricing and deposit rules. `Economy` and `Premium` implement it.                                                                  |
| `RenterType`, `RentalStatus` | Enums, so a typo is a compile error and not a silent bug.                                                                                               |
| `RentalRefusedException`     | Checked exception for refusals at the counter (see below).                                                                                              |
| `Ledger`                     | The improvement beyond the brief (below).                                                                                                               |
| `app.Main`, `app.Report`     | The demo, and the printing of the end-of-day report. Presentation only; `Report` uses `Depot`'s read-only views.                                        |

## Design decisions

**Status is derived, not stored.** The original kept "is it out?" in two places
(`Vehicle.status` and `Rental.status`) and nothing kept them in agreement. Now a vehicle is
rented if and only if an `ACTIVE` rental exists for its plate (`Depot.isRented`). With a single
record there is nothing to contradict. This was chosen over keeping a status field and checking
it carefully, because a stored field can always be forgotten or bypassed.

**`Depot` is the only gatekeeper.** `rent` checks the vehicle, the renter, whether the car is
already out and the number of days **before** changing anything, and refuses if a check fails.
`returnVehicle` refuses a car that is not out. A failed call changes nothing. Every public method
of `Depot` is `synchronized`, so the check "is it already out?" and the change that follows cannot be
split by another thread.

**Refusals are a checked exception.** A car already being out, an unknown plate or a bad number of
days are expected events at the counter that the caller can recover from, so `rent` and
`returnVehicle` throw the checked `RentalRefusedException` and the compiler forces the caller to
handle it. Programming mistakes (a null name, an invalid object) stay unchecked. `Main` catches
only `RentalRefusedException`, not `RuntimeException`, so a real bug is not hidden behind a
friendly message.

**Encapsulation.** Every field is private and final where it can be. `getRentals()` already
existed in the original and returned the real internal list. It stays, so no feature is lost,
but it now returns `List.copyOf(...)`, a snapshot. Callers cannot add or remove rentals through
it, and `Rental` has no public mutators. `Main` is in a different package from the domain classes
on purpose: the package-private `markReturned()` and `Ledger.record()` really are out of its
reach (Java does not allow calls to package-private methods from another package).

**Presentation at the edge.** `Depot` no longer prints. It exposes read-only views (`fleet()`,
`activeRentals()`, `findRenter(id)`, `isRented(plate)`), and `app.Report` does the printing, so
the rules can be reused without a console.

**Records** are used for `Vehicle` and `Renter`: they are pure immutable values, so the record
replaces the constructor, accessors and `equals`/`hashCode`, and the compact constructor is the one
place the checks live. `Rental` stays a class because its status changes.

**Types instead of strings.** Renter type and rental status are enums. Categories are a
`Category` interface. In the original, `"Member"` instead of `"MEMBER"` silently charged a full
deposit.

**Links by id, and one record per car.** Rentals link to the renter by id, not by name. The fleet
is a map keyed by the plate (trimmed and upper-cased), so the same car cannot be registered twice.
The plate that is printed stays exactly as entered.

**Money is `BigDecimal`**, not `double`. `Report.money(...)` formats it so the output matches the
original (`450.0`, `0.0`, `450.5`).

### How this makes the commercial van easy to add

A van is one new file and **no edits to any existing class**:

```java
public final class CommercialVan implements Category {
    @Override public BigDecimal cost(BigDecimal rate, int days) { return rate.multiply(BigDecimal.valueOf(days)); }
    @Override public BigDecimal deposit(BigDecimal baseDeposit, RenterType renterType) { /* the van's own deposit rule */ return baseDeposit; }
    @Override public String name() { return "COMMERCIAL_VAN"; }
}
```

`Depot.rent` already calls `category.cost(...)` and `category.deposit(...)`, so it prices the van
without knowing the van exists. A van cannot be added without implementing both
rules, because the compiler enforces the interface.

**What the old version would have forced:** a third copy-pasted `else if (v.category.equals("VAN"))`
branch inside `rent`, with its own copy of the deposit logic. The shared lines under the branches
(`history.add`, `rentals.add`) would have run for any category that matched nothing, so a typo in
the category would have created a GHS 0 rental, silently. Every other place that compares
category strings would also have needed checking.

### Changes from the design sketch

DIAGNOSIS.md was committed before the first code change, so it is left as it was. While building
I changed these things, and why:

- `printReport()` moved out of `Depot` into `app.Report`. The sketch kept it on `Depot`, but a
  domain class that prints is the separation-of-concerns problem the diagnosis itself lists.
- `Main` moved to its own package, so the package-private methods cannot be called from it.
- Refusals use a checked `RentalRefusedException`, which the sketch only described as "throws".
- `Vehicle` and `Renter` became records.
- `Depot` gained read-only views (`fleet()`, `activeRentals()`, `findRenter()`) for `Report`, and
  its public methods are `synchronized`.

### Known limits

- Rentals and ledger entries are kept in memory and only ever grow. That is fine at one depot's
  volume, but a long-lived tool would need to archive old rentals.
- `Vehicle` is a record, so its `equals` compares the `Category` object, and `Economy` has no
  `equals`. Vehicles are never compared or used as map keys (the fleet is keyed by plate), so this
  has no effect today.

### Trade-off I accepted

`Economy` and `Premium` currently have identical rules, so they look like duplicates. I kept them
as separate classes because the business treats them as different classes of vehicle, and the van
is going to need its own rules. Sharing code between them now would couple the rules together,
which is the original problem (a premium tweak breaking economy).

## Behaviour preservation

The baseline output was captured from the original program before any change
(`baseline-output.txt`). The rebuilt `Main` produces the same report, with these **intended
differences** (also listed in DIAGNOSIS.md, section 4):

1. The demo's last rent (the Vitz is already out with Kofi) is refused. `Main` prints the message
   `Vehicle already rented: GT-1234-20`, and Rental #3 no longer appears in the report.
2. A refused attempt does not consume a rental number.
3. Plates are matched ignoring case and surrounding spaces (printing is unchanged).
4. The `Ledger:` block is printed at the end.

**Deliberate deviations from the original** (the diagnosis said these would be flagged here):

- A vehicle with a rate of zero or less, or a negative deposit, cannot be created.
- `days` below 1 is rejected. The original accepted zero and negative days (GHS -300.0 for -2 days).
- Renting an unknown vehicle or renter, or returning a car that is not rented, now throws and
  prints a message. The original printed "Returned" for a car that was never out.
- A return closes the one active rental for that car. The original closed every active rental
  with that plate, which could never happen correctly.

### Evidence

Original (`baseline-output.txt`):

```text
Rented Toyota Vitz (GT-1234-20) to Kofi Mensah for 3 days, cost GHS 450.0, deposit GHS 500.0
Rented Toyota Prado (GR-5678-22) to Ama Owusu for 2 days, cost GHS 1200.0, deposit GHS 0.0
Returned Toyota Prado (GR-5678-22)
Rented Toyota Vitz (GT-1234-20) to Ama Owusu for 2 days, cost GHS 300.0, deposit GHS 0.0

=== Dovewing Car Rentals - end of day ===
Fleet status:
  Toyota Vitz (GT-1234-20, ECONOMY): RENTED
  Toyota Prado (GR-5678-22, PREMIUM): AVAILABLE
Active rentals:
  Rental #1: GT-1234-20 -> Kofi Mensah (3 days)
  Rental #3: GT-1234-20 -> Ama Owusu (2 days)
```

Rebuild (`com.amalitech.dovewing.app.Main`):

```text
Rented Toyota Vitz (GT-1234-20) to Kofi Mensah for 3 days, cost GHS 450.0, deposit GHS 500.0
Rented Toyota Prado (GR-5678-22) to Ama Owusu for 2 days, cost GHS 1200.0, deposit GHS 0.0
Returned Toyota Prado (GR-5678-22)
Vehicle already rented: GT-1234-20

=== Dovewing Car Rentals - end of day ===
Fleet status:
  Toyota Vitz (GT-1234-20, ECONOMY): RENTED
  Toyota Prado (GR-5678-22, PREMIUM): AVAILABLE
Active rentals:
  Rental #1: GT-1234-20 -> Kofi Mensah (3 days)
Ledger:
  events: 4
  active rentals: 1
  status: OK
```

The first three lines and the whole fleet status are identical. The differences are the intended
ones above.

## The improvement beyond the brief: `Ledger`

**Why:** staff keep a paper backup because they do not trust the screen. The ledger replaces the
paper. `Depot` records every attempt (rents, returns and **refusals**) with a timestamp. A
refusal never appears in the rentals list, so before this there was no record of who tried to
take a car that was already out, or why it was refused.

**The check:** `Ledger.report(...)` compares its own records with the live rentals. The number of
`RENT` lines must equal the total number of rentals, and the number of `RETURN` lines must equal
the number of returned rentals. If not, it throws `IllegalStateException`. So if anything ever
changes a rental without going through `Depot`, the tool fails loudly instead of showing a
screen that quietly lies, which is the failure this depot already suffered from.

Events are stored as typed entries (`RENT`, `RETURN`, `REFUSE`), and the check counts them by type,
not by searching text. It is plain Java with no framework, `record(...)` is package-private so code
outside the package cannot write to it, and `entries()` returns an unmodifiable copy.
