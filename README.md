# Air Quality Monitor
**Air Quality Monitor** è un applicazione che nasce dalla necessità di **visualizzare** e **monitorare** i dati relativi alla **qualità dell'aria** italiani, in rapporto alle **cause di morte**. Il tool permette di 
caricare i dati a partire da file **csv**, creare **grafici** e calcolarne la **correlazione**. Inoltre, c'è la possibilità di generare **report** in cui è visualizzabile la correlazione tra inquinante e cause di 
morte con relativa descrizione dell'operatore. 


Si è reso disponibile anche un semplice tool per monitorare la quantità di **Monossido di Carbonio (CO)** presente nell'aria, attraverso comunicazione **UART** via USB (LOINC codes compliant).


## Guida per l'installazione
Per prima cosa, andare alla pagina di [download](https://github.com/D1D02/air-quality-monitor/releases/tag/0.1.0) e scaricare la release.
Una volta scaricata, se non si ha Java 17 bisogna scaricare dal [sito ufficiale](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) la release.


## Input
I csv contenenti i **dati dell'aria** vanno formattati in questo modo:
```
_id,STAZIONE,DATA_RILEVAZIONE,CODICE_INQUINANTE,UNITA_MISURA,VALORE_INQUINANTE
1,IT2219A,2020/01/02 00:00:00.000000000,C6H6,µg/m3,"4,21168600"
2,IT1504A,2020/01/02 00:00:00.000000000,CO,mg/m3,"0,26472032"
3,IT1491A,2020/01/02 00:00:00.000000000,NO2,µg/m3,"33,87974500"
4,IT1491A,2020/01/02 00:00:00.000000000,PM10,µg/m3,"24,80000000"
5,IT2214A,2020/01/02 00:00:00.000000000,NO2,µg/m3,"8,78384000"
6,IT2214A,2020/01/02 00:00:00.000000000,C6H6,µg/m3,"0,41774210"
```



I csv contenenti i **dati sulla mortalità** vanno formattati in questo modo:
```
Tempo  ;2006  ;2007  ;2008  ;2009  ;2010  ;2011  ;2012  ;2013  ;2014  ;2015  ;2016  ;2017  ;2018  ;2019  ;2020  ;2021  ;2022  
Indicatore  ;Morti  ;Morti  ;Morti  ;Morti  ;Morti  ;Morti  ;Morti  ;Morti  ;Morti  ;Morti  ;Morti  ;Morti  ;Morti  ;Morti  ;Morti  ;Morti  ;Morti  
Causa iniziale di morte - European Short List  ;  ;  ;  ;  ;  ;  ;  ;  ;  ;  ;  ;  ;  ;  ;  ;  ;  
Di cui tumori maligni della laringe  ;184;177;162;195;203;185;182;168;163;169;181;220;166;191;194;163;163
Malattie del sangue e degli organi ematopoietici ed alcuni disturbi del sistema immunitario  ;185;206;201;175;184;203;192;201;234;246;226;255;246;287;248;299;305
Diabete mellito  ;2.335;2.496;2.591;2.621;2.637;2.694;2.682;2.767;2.693;3.006;2.886;3.076;2.835;2.865;3.186;3.353;3.369
Malattia di alzheimer  ;490;594;558;563;608;646;681;687;684;859;712;863;740;827;779;800;800
Di cui infarto miocardico acuto  ;2.756;2.755;2.707;2.712;2.605;2.584;2.467;2.397;2.339;2.469;2.135;2.103;1.924;1.912;1.958;1.839;1.778
Di cui altre malattie ischemiche del cuore  ;3.817;3.996;4.034;4.135;4.331;4.694;4.693;4.733;4.699;5.269;4.852;4.906;4.642;4.655;4.830;4.697;4.921
Altre malattie del cuore  ;3.705;3.943;3.709;3.730;3.633;3.706;3.784;4.088;4.013;4.753;4.304;4.589;4.441;4.650;3.631;3.752;3.865
Malattie cerebrovascolari  ;5.949;6.082;6.223;6.270;5.978;6.112;6.048;5.653;5.646;6.203;5.742;6.011;5.422;5.414;5.517;5.351;5.378
Altre malattie del sistema circolatorio  ;3.235;3.497;3.512;3.725;3.873;3.989;4.187;4.198;4.205;4.730;4.314;4.757;4.412;4.502;5.020;4.981;5.250
Polmonite  ;204;209;228;227;201;236;274;291;271;353;326;450;468;557;559;509;553
Malattie croniche delle basse vie respiratorie  ;2.009;2.115;2.075;2.139;2.147;2.086;2.104;2.064;1.935;2.192;2.463;2.642;2.464;2.646;2.614;2.388;2.637
Altre malattie del sistema respiratorio  ;623;669;668;721;700;830;829;852;857;929;891;995;1.054;1.039;1.162;1.188;1.275
Covid-19  ;..;..;..;..;..;..;..;..;..;..;..;..;..;..;3.719;5.501;4.449
```

## Output
### Interfaccia Iniziale
Il programma si dovrebbe presentare in questo modo. Da qui, si possono caricare i dati usando i relativi tasti all'interno dell'interfaccia:


<img width="1170" height="764" alt="image" src="https://github.com/user-attachments/assets/84c9b216-3df2-4546-80bc-ce8bd2a24d82" />


### Grafico
Selezionando la regione ed il periodo di riferimento, cliccando su **Genera PDF** si ottiene il pdf con i dati di correlazione, e cliccando **Genera Grafico** si arriva a quest'altra schermata in cui si possono 
confrontare i dati e visualizzarne la correlazione:


<img width="1234" height="864" alt="image" src="https://github.com/user-attachments/assets/a0e40131-c770-4d2a-97eb-0c79d93bd8a6" />


### Grafico Monossido Carbonio
Cliccando **Monitor UART** si può monitorare i dati mandati sulla seriale da un dispositivo IOT compatibile, che manda i dati secondo lo standard *LOINC* 
(_93045-3: Carbon monoxide [Mass/volume] in Air_), selezionando il dispositivo tra quelli disponibil e cliccando **Connetti**:


<img width="1106" height="741" alt="image" src="https://github.com/user-attachments/assets/13007ba6-1848-4a40-942b-438c4eb7e73d" />



 
