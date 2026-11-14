# Gestionnaire de finances personnelles

## But

Suivre un compte courant : revenus, dépenses, budget mensuel par catégorie et objectif d'épargne. Une dépense qui dépasse le budget de sa catégorie ou le solde du compte est refusée.

## Lancement

Java 21 et Maven, depuis la racine du projet :

```bash
mvn clean package
java -cp target/classes com.project.Application
```

Un autre fichier de données peut être passé en argument.

## Fonctionnalités

- Chargement du compte, des budgets, de l'objectif et des transactions depuis un CSV.
- Refus d'une dépense si le budget mensuel de sa catégorie ou le solde est dépassé.
- Rapport des lignes écartées au chargement, avec la raison.
- Relevé de compte.
- Bilan par mois : dépenses, épargne, plus grosse dépense, état des budgets (alerte à partir de 80 %).
- Progression vers l'objectif d'épargne.

## Classes principales

- `model` : `Account`, `Transaction` (abstraite), `Expense`, `Income`, `Budget`, `SavingsGoal`, `ExpenseCategory` (enum).
- `service` : `FinanceService` (enregistre les transactions en appliquant les règles), `BudgetService` (budgets), `SpendingAnalyser` (calculs mensuels), `TransactionRecorder` (interface).
- `utils` : `CsvDataLoader`, `ImportReport`.
- `ui` : `ReportPrinter` (affichage console).
- `Application` : le `main`, qui assemble le tout.

## Décisions techniques

1. Un solde insuffisant, un budget dépassé ou un fichier illisible peuvent arriver en fonctionnement normal, donc je veux que le compilateur oblige à les gérer. Un montant négatif est une erreur de l'appelant, donc `InvalidAmountException` hérite de `RuntimeException`. 
2. Le budget et le solde sont contrôlés avant de toucher au compte, donc une dépense refusée ne change rien.
3. Une ligne de configuration fausse (compte, budget, objectif) arrête l'application, parce qu'elle fausserait tous les calculs. Une transaction fausse est seulement signalée et les suivantes sont chargées, pour qu'une seule erreur de saisie ne fasse pas perdre tout l'historique.
4. Tolérance d'arrondi dans `Budget.allows` avec des `double`, `0.1 + 0.2` dépasse `0.3`, ce qui refusait à tort une dépense atteignant exactement le plafond.
5. Objectif d'épargne calculé sur le solde du compte, parce que c'est l'argent réellement disponible. La progression est plafonnée à 100 % pour ne pas afficher 120 %, et l'objectif est facultatif pour qu'un fichier sans ligne `GOAL` reste valide.

## Héritage

`Expense` et `Income` héritent de `Transaction`, qui porte le montant, la date, la description et leur validation.

## Polymorphisme

`applyTo`, `getSignedAmount` et `getLabel` sont abstraites dans `Transaction` et redéfinies dans `Expense` et `Income`. `FinanceService.record` appelle `applyTo` : une dépense fait un retrait, un revenu fait un dépôt. `ReportPrinter.printStatement` et `SpendingAnalyser.netCashFlow` parcourent une `List<Transaction>` en appelant `getLabel` et `getSignedAmount`, sans tester le type (aucun `instanceof`).

## Classe abstraite et interface

- `Transaction` est abstraite : les sous-classes partagent des attributs et du code, et une transaction est forcément une dépense ou un revenu.
- `TransactionRecorder` est une interface : `CsvDataLoader` a seulement besoin d'enregistrer des transactions, pas de connaître `FinanceService`.

## Exceptions personnalisées

- `InvalidAmountException` (`RuntimeException`) : montant nul ou négatif.
- `InsufficientBalanceException` (`Exception`) : retrait ou dépense supérieur au solde.
- `BudgetExceededException` (`Exception`) : budget mensuel de la catégorie dépassé.
- `DataLoadException` (`Exception`) : fichier illisible ou ligne mal formée.

## Chargement des données

`CsvDataLoader` lit `src/main/resources/data.csv` (séparateur `;`) :

```
ACCOUNT;numéro;titulaire;solde initial
BUDGET;catégorie;plafond mensuel
GOAL;montant à épargner
INCOME;date;montant;description;source
EXPENSE;date;montant;description;catégorie
```

Le compte, les budgets et l'objectif sont créés d'abord, puis chaque transaction passe par `FinanceService`. Le fichier contient exprès cinq lignes fausses pour montrer les rejets.

## SOLID

- S : lecture (`CsvDataLoader`), règles (`FinanceService`, `BudgetService`), calculs (`SpendingAnalyser`) et affichage (`ReportPrinter`) sont dans des classes séparées.
- O : le relevé et le calcul de l'épargne passent par les méthodes abstraites de `Transaction`. Limite : un nouveau type de transaction demanderait quand même une méthode `record` de plus.
- L : `Income.applyTo` ne lève pas l'exception déclarée par la méthode parente, ce qui reste compatible.
- I : `TransactionRecorder` ne contient que les deux méthodes `record`.
- D : `CsvDataLoader` dépend de l'interface `TransactionRecorder`, pas de `FinanceService`. `FinanceService` reçoit ses objets par son constructeur (injection de dépendances).

## Tests

```bash
mvn test
```

63 tests JUnit 5.