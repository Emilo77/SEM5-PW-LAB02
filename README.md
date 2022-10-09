# Laboratorium 2 - kończenie wątków

Tematem zajęć jest mechanizm przerwań oraz czekanie na zakończenie wątku.

Do scenariusza dołączone są programy przykładowe:

- [przyklady02/Interrupts.java](https://github.com/Emilo77/SEM5-PW-LAB2/blob/master/przyklady02/Interrupts.java)

- [przyklady02/TwoWritersPeterson.java](https://github.com/Emilo77/SEM5-PW-LAB2/blob/master/przyklady02/TwoWritersPeterson.java)

- [przyklady02/RogueThread.java](https://github.com/Emilo77/SEM5-PW-LAB2/blob/master/przyklady02/RogueThread.java)

- [przyklady02/SpawnableWorkers.java](https://github.com/Emilo77/SEM5-PW-LAB2/blob/master/przyklady02/SpawnableWorkers.java)

- [przyklady02/UnresponsiveFileDownloader.java](https://github.com/Emilo77/SEM5-PW-LAB2/blob/master/przyklady02/UnresponsiveFileDownloader.java)

- [przyklady02/ChildException.java](https://github.com/Emilo77/SEM5-PW-LAB2/blob/master/przyklady02/ChildException.java)

### Czekanie na zakończenie wątku

W programach przykładowych do poprzednich zajęć, wątki aktywnie czekały na zakończenie innych wątków. Takie rozwiązanie powoduje marnowanie czasu procesora.

Można próbować to naprawić, wywołując w pętli oczekującej statyczną metodę `Thread.yield()`. W niektórych implementacjach maszyny wirtualnej Javy może to spowodować oddanie przez wątek reszty przydzielonego mu kwantu czasu procesora. Nie ma jednak gwarancji, że rzeczywiście tak się stanie.

Alternatywnym rozwiązaniem może być wywołanie w pętli metody `Thread.sleep(int millis)`, która usypia wątek na wskazaną argumentem `millis` liczbę milisekund. Wątek, który śpi, nie obciąża procesora.

Jeśli wątek czekający na zakończenie innego wątku nie ma w tym czasie nic innego do zrobienia, zdecydowanie najlepszym rozwiązaniem jest użycie metody `join()`. Powoduje ona zawieszenie wykonującego ją wątku do czasu zakończenia pracy przez wątek, na rzecz którego została wywołana. Innymi słowy, jeśli wątek `A` napotka instrukcję `B.join()`, to poczeka, aż wątek `B` skończy się wykonywać.

### Wątki – motywacja

Nowym wątkom zazwyczaj zlecamy czasochłonne obliczenia, których wykonywanie w głównym wątku powodowałoby jego zablokowanie. Przykładem może być aplikacja do pobierania plików z interfejsem użytkownika wyświetlającym postępy. Ponieważ operacje I/O (komunikacja przez sieć, czytanie i zapisywanie plików) trwają długo, nie powinny być wykonywane w wątku obsługującym interfejs użytkownika, gdyż sprawi to, że aplikacja stanie się nieresponsywna.

W aplikacji [przyklady02/UnresponsiveFileDownloader.java](https://github.com/Emilo77/SEM5-PW-LAB2/blob/master/przyklady02/UnresponsiveFileDownloader.java) możemy zaobserwować powyższy problem. Główny wątek zajmuje się sprawdzaniem inputu od użytkownika i wyświetlaniem informacji o aktualnym czasie i postępach. Po naciśnięciu entera rozpoczyna się pobieranie pliku, dla uproszczenia symulowane przez metodę `Thread.sleep`. Widzimy, że aplikacja przestaje się odświeżać w czasie pobierania pliku.

### Wcześniejsze kończenie

Czasami chcemy przerwać pracę wątku przed jego normalnym zakończeniem. Posłużmy się raz jeszcze przykładem aplikacji do pobierania plików: może być tak, że użytkownik w połowie pobierania stwierdza, że jednak nie potrzebuje danego pliku. Aplikacja powinna zapewniać możliwość anulowania pobierania. W sytuacji jednowątkowej moglibyśmy sprawdzać co jakiś czas flagę, która informuje o tym, czy należy zatrzymać obliczenia. Okazuje się, że do obsługi przerwań analogiczny mechanizm wykorzystują wątki w Javie.

### Przerwania

Wątek w Javie można przerwać, wywołując na jego obiekcie metodę `interrupt()`. Jej działanie jest banalne: podnosi ona w obiekcie wątku flagę `interrupted`. Wartość flagi można sprawdzić przy użyciu metody `isInterrupted()`. Czy samo ustawienie tej flagi wystarcza do zakończenia pracy wątku? Nie! Aby metoda `interrupt()` przyniosła zamierzony efekt, wątek (a konkretniej: zlecona mu praca) musi odpowiednio obsługiwać przerwania. Oznacza to konieczność sprawdzania – w dogodnych z punktu widzenia wykonywanej pracy momentach – flagi `interrupted`. W przypadku aplikacji do pobierania plików dogodny moment będzie po pobraniu każdego odpowiednio małego fragmentu danych. Wątek poinformowany o tym, że powinien zakończyć pracę, ma okazję, aby po sobie posprzątać – np. zamknąć pliki i połączenia, które otworzył. Chodzi o to, aby obiekty, na których pracował, zostały pozostawione w dobrze określonym stanie.

Obejrzyjmy teraz przykład aplikacji uruchamiającej wątek, który nie obsługuje przerwań: [przyklady02/RogueThread.java](https://github.com/Emilo77/SEM5-PW-LAB2/blob/master/przyklady02/RogueThread.java). Wątek ten nie sprawdza w żaden sposób flagi przerwania, więc pozostaje głuchy na prośby głównego wątku o zakończenie pracy. Czy oznacza to, że jesteśmy bezbronni w stosunku do tego rodzaju łobuzów? Niestety tak. Methoda `interrupt` jest jedynym sposobem na bezpieczne zatrzymanie wątku. Z dawnych czasów ostała się jeszcze przestarzała metoda `Thread.stop()`, która pozwala na natychmiastowe zabicie wątku, ale jej użycie jest zdecydowanie odradzane, ponieważ nie daje ona okazji wątkom na sprzątnięcie po sobie. Zainteresowanych odsyłamy do (nomen omen) [wątku na StackOverflow](https://stackoverflow.com/questions/5241822/is-there-a-good-way-to-forcefully-stop-a-java-thread) i [dokumentacji](https://docs.oracle.com/javase/8/docs/technotes/guides/concurrency/threadPrimitiveDeprecation.html).

### Praca i sen

Dowiedzieliśmy się już, w jaki sposób można przerwać pracujący wątek. Ale co zrobić, jeśli wątek w danym momencie śpi (bo ktoś wywołał na nim `sleep()` lub `join()`)? Śpiący wątek nie ma przecież jak sprawdzać flagi `interrupted`. Dlatego właśnie metody, które zawieszają pracę wątków, zgłaszają kontrolowany (checked) wyjątek `InterruptedException`. Wątek, który z tych metod korzysta, musi więc obsłużyć wyjątek lub zadeklarować, za pomocą `throws`, że tego nie robi (przypomnienie: jest to cecha wyjątków kontrolowanych). Wyjątek ten jest rzucany automatycznie przez maszynę wirtualną w śpiącym wątku, gdy ktoś spróbuje go przerwać. Dzięki temu wątek zostaje wybudzony i może prawidłowo obsłużyć swoje przerwanie. Jednakże nie dzieje się to automatycznie: wątek wybudzony wyjątkiem `InterruptedException` powinien wywołać (na sobie) metodę `interrupt()`, aby ustawić sobie flagę przerwania i tym samym rozpocząć procedurę obsługi przerwania analogiczną do przerwania w czasie pracy.

Program przykładowy [przyklady02/Interrupts.java](https://github.com/Emilo77/SEM5-PW-LAB2/blob/master/przyklady02/Interrupts.java) demonstruje obsługę przerwań. Na zakończenie jednego wątku czekamy, drugi jest przerywany podczas snu, a trzeci podczas obliczeń.

### Uwagi

Zgłoszenie wyjątku `InterruptedException` powoduje jednocześnie skasowanie flagi przerwania, jeżeli była ona wcześniej podniesiona.

Stan flagi przerwań wątku, który aktualnie się wykonuje, sprawdzamy statyczną metodą `Thread.interrupted()`. Daje ona wartość flagi i jednocześnie, tak jak w przypadku zgłoszenia wyjątku `InterruptedException`, kasuje flagę.

Co zrobić, jeśli kod obsługujący przerwania znajduje się w metodzie run(), ale w tej samej metodzie wywołujemy jakieś metody, które mogą trwać długo, więc tym samym powinny prawidłowo obsługiwać przerwania? Otóż do takich właśnie sytuacji służą wyjątki. Metody te powinny rzucać wyjątek `InterruptedException`, gdy zauważą przerwanie. Dzięki wyjątkom obsługa sytuacji wyjątkowych może zostać przekazana do funkcji znajdujących się wyżej w hierarchii wywołań.

run() w Runnable nie może deklarować `throws` (gdyż taka deklaracja stanowi część interfejsu). Każdy wątek powinien obsługiwać swoje wyjątki, ponieważ wyjątki nie propagują się pomiędzy wątkami! Jest tak dlatego, że każdy wątek posiada osobny stos. Przykład wątku, który niepostrzeżenie umiera wskutek nieobsłużegnia wyjątku, znajdziemy w programie [przyklady02/ChildException.java](https://github.com/Emilo77/SEM5-PW-LAB2/blob/master/przyklady02/ChildException.java).

Program [przyklady02/TwoWritersPeterson.java](https://github.com/Emilo77/SEM5-PW-LAB2/blob/master/przyklady02/TwoWritersPeterson.java) jest modyfikacją programu piszącego litery i cyfry z poprzednich zajęć. Tym razem oczekiwanie na zakończenie wątku jest zrealizowane za pomocą metody `join()`. Do synchronizacji wątków, gwarantującej, że w każdym wierszu będą albo tylko litery, albo tylko cyfry, użyto algorytmu Petersona.

Program [przyklady02/SpawnableWorkers.java](https://github.com/Emilo77/SEM5-PW-LAB2/blob/master/przyklady02/SpawnableWorkers.java) pozwala na dynamiczne tworzenie wątków oraz ich przerywanie z poziomu konsoli.

### Ćwiczenie niepunktowane (Primes2)

Zmodyfikuj rozwiązanie ćwiczenia z poprzednich zajęć, dotyczącego liczb pierwszych. Wprowadź do implementacji oczekiwanie na zakończenie wątków pomocniczych za pomocą `join()` oraz obsługę przerwań.

### Ćwiczenie niepunktowane (ResponsiveFileDownloader)

Przerób aplikację `UnresponsiveFileDownloader` w taki sposób, aby "pobieranie" odbywało się w osobnym wątku. Dodaj możliwość przerwania rozpoczętego pobierania i zrestartowania zakończonego pobierania po ponownym naciśnięciu enter.
Ćwiczenie punktowane (Vectors)

Zdefiniuj klasę `Vector`, której obiekty będą reprezentowały wektory zadanej długości.

Zaimplementuj w niej metodę `Vector sum(Vector other)` liczącą sumę danego wektora z drugim o tej długości oraz metodę `int dot(Vector other)` liczącą ich iloczyn skalarny.

W obu metodach obliczenie zrealizuj wielowątkowo. Zlecaj wątkom pomocniczym dodawanie lub mnożenie fragmentów wektorów o długości 10.

Zdefiniuj metodę `main()` demonstrującą, na losowych danych, że dodawanie i mnożenie wektorów działa prawidłowo.

Dla sprawdzenia poprawności zdefiniuj metody `sumSeq` i `dotSeq` analogiczne do wcześniejszych, ale działające sekwencyjnie (czyli bez użycia dodatkowych wątków).

Czekanie na zakończenie pracy przez wątki pomocnicze zrealizuj za pomocą metody `join()`. Zadbaj o prawidłową obsługę przerwań.
