System zarządzania incydentami (mini-ITIL)
    Projekt polega na zaprojektowaniu i zbudowaniu minimalistycznej platformy do zarządzania incydentami operacyjnymi w organizacji świadczącej usługi wsparcia IT. System umożliwia zgłaszanie incydentów, ich automatyczną wstępną klasyfikację z wykorzystaniem modelu językowego (LLM), zarządzanie cyklem życia zgłoszeń oraz monitorowanie zobowiązań SLA wynikających z umów wsparcia dla różnych projektów i poziomów usług. Szczególną uwagę poświęca się obsłudze incydentów krytycznych, które wymagają intensywnej koordynacji działań zespołu, planowanych spotkań operacyjnych oraz obowiązkowego raportu post mortem po zakończeniu. System rejestruje historię zdarzeń, wspiera eskalacje przy zagrożeniu przekroczenia SLA oraz umożliwia naliczanie kar kontraktowych w przypadku naruszenia zobowiązań serwisowych. Celem projektu jest stworzenie rozwiązania, które modeluje rzeczywiste procesy, a jednocześnie stanowi dobre pole do pracy zespołowej nad projektowaniem domeny, architekturą zdarzeniową i integracją z usługami AI.
    
1. Jako klient wsparcia chcę zgłosić incydent poprzez API lub formularz, aby rozpocząć jego obsługę.

System generuje unikalny identyfikator dla każdego incydentu.
System zapisuje pełny opis incydentu i metadane (czas, kanał, autor).
Zgłoszenie inicjuje stan „Nowy" w lifecycle incydentu.
Audytowalność: każda akcja zapisana w historii.

2. Jako agent wsparcia chcę przeglądać listę incydentów wraz z ich statusem i priorytetem, aby wiedzieć nad czym pracować.

Widok listy incydentów z filtrowaniem po stanie, priorytecie, kategorii.
Możliwość sortowania i przeszukiwania.

3. Jako kierownik wsparcia chcę widzieć pełną historię zdarzeń incydentu, aby móc odtworzyć przebieg decyzji.

Wyświetlenie wszystkich zmian stanu, decyzji klasyfikacji, eskalacji i SLA.
Możliwość filtrowania historii według daty i typu zdarzenia.

4. Jako agent wsparcia chcę otrzymać rekomendowaną kategorię i priorytet incydentu, aby szybciej podjąć decyzję.

Integracja z LLM do wstępnej klasyfikacji.
Zapis rekomendacji jako zdarzenia w historii incydentu.
Możliwość odczytu rekomendacji bez zmiany stanu incydentu.

5. Jako agent wsparcia chcę móc zatwierdzić lub zmodyfikować rekomendację klasyfikacji, aby odpowiadała rzeczywistej sytuacji.

Możliwość nadpisania rekomendacji LLM.
Zapis ostatecznej decyzji w historii incydentu.
Powiązanie zmiany klasyfikacji z wpływem na SLA.