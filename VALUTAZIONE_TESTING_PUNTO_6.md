# Valutazione del punto 6: testing black-box dei casi d'uso principali

## Perimetro

Questa valutazione riguarda i test realizzati rispetto alla consegna funzionale
`Elaborato2025_26_def.pdf` e alle linee guida di `10-Testing.pdf`.

Il nuovo set di test e' stato impostato sui casi d'uso principali delle cinque versioni
incrementali dell'applicazione:

- configurazione di campi e categorie da parte del configuratore;
- creazione, validazione e pubblicazione delle proposte;
- registrazione fruitore, consultazione bacheca, adesione e spazio personale;
- disdetta, ritiro e passaggi di stato automatici;
- import batch di campi, categorie e proposte.

I test sono black-box funzionali: derivano dai requisiti e osservano il comportamento
attraverso le API applicative pubbliche usate dai controller (`AuthenticationService`,
`ConfigurationService`, `ProposalService`, `ProposalLifecycleService`,
`BatchImportService`). Non sono stati derivati da cammini interni, copertura del codice o
ordine degli `if`.

## Linee guida applicate

Dal materiale `10-Testing.pdf` sono state applicate queste indicazioni:

- considerare tutte le funzionalita' principali senza moltiplicare casi ridondanti;
- usare partizionamento in classi di equivalenza;
- usare analisi dei valori limite dove il requisito definisce confini temporali o numerici;
- includere input non validi e file malformati per mettere in difficolta' il sistema;
- mantenere i test automatici in JUnit come test di regressione.

## Codice dei test

Il codice principale introdotto o riscritto si trova in:

```text
test/it/unibs/ingesw/test/BlackBoxTestSupport.java
test/it/unibs/ingesw/test/ConfigurationServiceFlowTest.java
test/it/unibs/ingesw/test/ProposalServiceFlowTest.java
test/it/unibs/ingesw/test/ApplicationLifecycleFlowTest.java
test/it/unibs/ingesw/test/BatchImportServiceFlowTest.java
```

`BlackBoxTestSupport` contiene solo fixture e dati di scenario. I test temporali
preparano uno stato persistente gia' esistente, come se l'applicazione fosse stata usata
nei giorni precedenti, poi verificano il comportamento passando dai servizi pubblici.

## Casi di test derivati

| Versione | Caso d'uso principale | Test JUnit | Tecnica black-box |
| --- | --- | --- | --- |
| 1 | Fissare campi base, comuni e categorie, con persistenza | `configuratorDefinesFieldsAndCategoriesOnceAndCanViewThemAfterReload` | equivalenza valida/non valida: primo salvataggio vs secondo salvataggio, nome libero vs duplicato |
| 1 | Modificare campi e rimuovere categorie | `configuratorUpdatesFieldSetsAndRemovesCategoriesUsingPublicOperations` | valori limite sugli indici: indice valido, indice fuori intervallo, indice negativo |
| 2 | Creare e pubblicare una proposta valida | `configuratorCreatesPublishesAndViewsProposalByCategory` | classe valida completa: tutti i campi obbligatori presenti e vincoli rispettati |
| 2 | Rifiutare la pubblicazione di una proposta non valida | `configuratorCannotPublishProposalOutsideValidDateEquivalenceClass` | valore limite: `Data = Termine + 1`, sotto il minimo richiesto di due giorni |
| 2 | Rifiutare una proposta con campo obbligatorio mancante | `configuratorCannotCreateProposalWithMissingMandatorySpecificField` | classe non valida: campo specifico obbligatorio assente |
| 3 | Registrare fruitore, visualizzare bacheca e aderire | `participantRegistersViewsBoardAndSubscribesRespectingUniquenessAndCapacity` | classi valide/non valide: username libero/occupato, capienza disponibile/esaurita, adesione ripetuta |
| 3 | Confermare o annullare proposte a scadenza e notificare | `expiredOpenProposalsBecomeConfirmedOrCanceledAndNotifySubscribers` | equivalenza sull'output: iscritti pari al numero richiesto vs iscritti insufficienti |
| 3 | Passare da confermata a conclusa | `confirmedProposalBecomesClosedAfterEndDate` | valore limite temporale: data conclusiva gia' superata |
| 3 | Cancellare notifiche nello spazio personale | `personalSpacePersistsAfterSelectiveNotificationRemoval` | equivalenza valida: rimozione selettiva e persistenza alla riapertura |
| 4 | Disdire e reiscriversi entro il termine | `participantCanCancelSubscriptionBeforeDeadlineAndSubscribeAgain` | classe valida: finestra iscrizioni aperta |
| 4 | Rifiutare disdetta dopo il termine | `participantCannotCancelSubscriptionAfterDeadlineBoundary` | valore limite temporale: termine iscrizione gia' superato |
| 4 | Ritirare una proposta prima dell'inizio, non nel giorno di inizio | `configuratorWithdrawsOpenProposalBeforeStartButNotOnStartDateBoundary` | boundary value: `oggi < Data` valido, `oggi = Data` non valido |
| 5 | Importare campi da file batch corretto | `importFieldsAcceptsCompleteConfigurationAndPersistsIt` | classe valida: file completo e coerente |
| 5 | Importare categorie in modo atomico per voce | `importCategoriesKeepsValidEntriesAndDiscardsInvalidEntriesIndependently` | classi equivalenti: categoria valida vs campo specifico in conflitto |
| 5 | Importare proposte valide, create e scartate | `importProposalsKeepsValidCreatedAndDiscardedEntriesSeparate` | classi equivalenti: proposta valida, proposta creata ma non validata, categoria inesistente, campo ignoto |
| 5 | Rifiutare file batch malformato senza modificare lo stato | `malformedBatchFileDoesNotModifyPersistedState` | input anomalo: errore di parsing e nessun effetto persistente |

Resta inoltre presente `ProposalRuleValidatorBlackBoxTest`, che esercita in modo piu'
mirato le classi di equivalenza e i valori limite del requisito "proposta valida": deadline
futura, distanza minima fra termine iscrizione e data evento, data conclusiva non
precedente, numero partecipanti positivo e quota non negativa.

## Motivazione black-box

I test non ispezionano la struttura interna dei metodi. Le asserzioni riguardano effetti
osservabili dal punto di vista dei casi d'uso:

- esiti booleani delle operazioni applicative;
- stato finale delle proposte;
- contenuto della bacheca;
- contenuto dell'archivio dopo ricaricamento;
- notifiche visibili nello spazio personale;
- report restituiti dall'import batch.

Quando un test deve simulare una proposta scaduta o una proposta gia' confermata, la
fixture scrive nel repository temporaneo uno stato di partenza coerente con il dominio. La
verifica rimane comunque black-box perche' l'esito e' osservato tramite i servizi pubblici.

## Verifica eseguita

Compilazione completa:

```text
javac -cp lib/junit-platform-console-standalone-6.0.3.jar:lib/gson-2.13.2.jar -d /private/tmp/ingesw-test-classes-pruned $(find src test -name '*.java' -print)
```

Esecuzione della suite JUnit:

```text
java -jar lib/junit-platform-console-standalone-6.0.3.jar execute --class-path /private/tmp/ingesw-test-classes-pruned:lib/gson-2.13.2.jar --scan-classpath --details=summary
```

Risultato:

```text
48 tests found
48 tests successful
0 tests failed
```

## Valutazione finale

Il punto richiesto e' soddisfatto con un set di test black-box piu' ampio del precedente:
non e' piu' coperto solo un metodo o una singola regola, ma i principali casi d'uso
dell'applicazione descritti dalla consegna 2025/26. I casi sono selettivi, per evitare
ridondanza, ma coprono sia classi valide sia classi non valide e includono i confini piu'
importanti: duplicati, indici fuori intervallo, scadenze, capienza, data di inizio e input
batch malformato.
