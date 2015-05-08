# Callas pdfaPilot

## Inledning
Dokument som publiceras till Alfresco Lager skall transformeras, om möjligt, till en PDF variant som är anpassad för långtidslagring. Den standarden kallas för PDF/A, och det finns flera olika versioner av standarden. Alfresco har i dagsläget ingen produkt som kan garantera korrekt konvertering, så detta måste göras med 3:e parts produkter. Callas pdfaPilot är marknadsledande på konverteringar till PDF/A och stödjer i dagsläget alla versioner.

## Uppsättning på VGR
PdfaPilot kan installeras och användas på ett flertal olika sätt. Den variant som VGR har valt är att använda pdfaPilot's 'satellite & dispatcher' uppsättning. Detta innebär att pdfaPilot installeras på en Microsoft Windows server och att pdfaPilot körs i serverläge. En klientdel installeras på de maskiner som skall initiera konverteringar.

## Installation av pdfaPilot klient
1. Ladda ner den korrekta klienten (Linux x64 för närvarande).
2. `mkdir /usr/local/callas_pdfaPilot_CLI_5_x64-5.2.221 && tar zxf ~/callas_pdfaPilotCLI_x64_Linux.tar.gz -C /usr/local/callas_pdfaPilot_CLI_5_x64-5.2.221 --strip-components=1`
3. `ln -s /usr/local/callas_pdfaPilot_CLI_5_x64-5.2.221 /usr/local/callas_pdfaPilot_CLI`
4. `ln -s /usr/local/callas_pdfaPilot_CLI/pdfaPilot /usr/local/bin/pdfaPilot`

Ändra på versionsnumret så att det passar önskad version.

## Installation av ny licens på server
Ny licensfil måste installeras när den gamla licens löper ut. Det är flera steg som måste genomföras, bl a skickade och mottagande av mail med licens. De två maskinerna som berörs är f n pdfapilot1.vgregion.se (huvudmaskin) & pdfapilot2.vgregion.se (coldspare maskin). De licenser som skall installeras är:

1. pdfaPilot Server 5 Unlimited -> huvudmaskin
2. pdfaPilot Dispatcher 5 -> huvud- och coldspare maskin
3. pdfaPilot Server 5 Coldspare Unlimited -> coldspare maskin

### Förberedande steg
1. Stoppa tjänsten `callas pdfaPilot Satellite`
2. Stoppa tjänsten `callas pdfaPilot Dispatcher`

### Installation av serverlicens (huvud- och coldspare maskin)

1. Logga in på maskinen med användaren pdfapilot
2. Starta en kommandoprompt med eleverad säkerhet
3. `cd "c:\program files\callas pdfaPilot Server 5\cli"`
4. `pdfaPilot.exe -k -o=email_server.txt "Peter Nyhlen" "Vastra Gotalandsregionen" <licenscode>`
5. Det har nu sparats ner ett dokument, email_server.txt, vars innehåll skall skickas som ett email till den mottagare som beskrivs i dokumentet. Enklaste sättet är att mappa upp katalogen och öppna filen med en texteditor, kopiera innehållet och skicka iväg mailet.
6. Ett svar kommar inom kort komma som innehåller ett dokument (License.txt) som skall sparas ner i samma katalog som pdfaPilot.exe ligger i.
7. Aktivera den nya licensen med `pdfaPilot.exe --activate License.txt`

### Installation av dispatcherlicens (huvud- och coldspare maskin)

1. Logga in på maskinen med användaren pdfapilot
2. Starta en kommandoprompt med eleverad säkerhet
3. `cd "c:\program files\callas pdfaPilot Server 5\cli"`
4. `pdfaPilot.exe -k -o=email_dispatcher.txt "Peter Nyhlen" "Vastra Gotalandsregionen" <licenscode>`
5. Det har nu sparats ner ett dokument, email_dispatcher.txt, vars innehåll skall skickas som ett email till den mottagare som beskrivs i dokumentet. Enklaste sättet är att mappa upp katalogen och öppna filen med en texteditor, kopiera innehållet och skicka iväg mailet.
6. Ett svar kommar inom kort komma som innehåller ett dokument (LicenseDispatcher.txt) som skall sparas ner i samma katalog som pdfaPilot.exe ligger i.
7. Aktivera den nya licensen med `pdfaPilot.exe --activate LicenseDispatcher.txt`

### Verifiering av licens
1. Exekvera `pdfaPilot.exe --status` för att kontrollera att licensen är korrekt installerad
2. Starta tjänsten `callas pdfaPilot Satellite`
3. Starta tjänsten `callas pdfaPilot Dispatcher`

## Länkar
Nedladdning: http://www.callassoftware.com/callas/doku.php/en:download
