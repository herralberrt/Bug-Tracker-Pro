# Tema2 POO

## Structura temei

Am organizat tema în mai multe pachete și clase, fiecare având diverse atribuții, precum:

- `main.commands` conține toate clasele pentru comenzi, fiecare implementând interfața Command.
- `main.ticket` include clasele pentru diferitele tipuri de tichete: Bug, FeatureRequest, UiFeedback.
- `main.milestone` gestionează milestone-urile, cu clase pentru definirea și manipularea acestora.
- `main.notif` se ocupă de notificări și de logica de observare a evenimentelor importante din aplicație.
- `main.utiliz` conține clasele pentru utilizatori: Developer, Manager, Reporter și logica asociată acestora.
- `main.enums` grupează toate tipurile de enum folosite pentru statusuri, priorități, roluri etc.
- `App` și `AppState` sunt clasele centrale, coordonând funcționarea generală a temei.

## Design patterns folosite

1. `Builder Pattern`: Am folosit acest pattern pentru crearea obiectelor de tip `Ticket`. Builder
pattern permite construirea tichetelelor cu multe atribute într-un mod clar, fără constructori
complicați sau parametri în exces.

2. `Command Pattern`: Am implementat acest pattern în pachetul `main.commands`, unde fiecare comandă
este o clasă care implementează interfața `Command`. Astfel, adăugarea sau modificarea comenzilor se
face ușor și structurat.

3. `Singleton Pattern`: Am folosit acest pattern pentru `CommandFactory` în pachetul `main.commands`,
implementat ca un enum. Astfel, există o singură instanță globală a fabricii de comenzi, accesibilă
din orice parte a aplicației.

4. `Factory Pattern`: Am folosit acest pattern în clasa `CommandFactory` din pachetul `main.commands`,
care creează instanțe de comenzi pentru clase din pachetele `main.utiliz`, `main.notif`, `main.ticket`
și `main.milestone`, pe baza inputului din JSON. Astfel, fiecare acțiune este gestionată centralizat.

5. `Observer Pattern`: Am implementat acest pattern în pachetul `main.notif`, unde notificările pentru
milestones sunt gestionate printr-un mecanism de observatori. Atunci când apar evenimente importante,
utilizatorii abonați la milestone primesc automat notificări, fără ca logica principală să depindă
direct de aceștia.
