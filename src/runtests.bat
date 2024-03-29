del *.class
java -jar jflex-1.6.1.jar Lexer.flex
javac *.java

java Program ../samples/fail_01.minc   > ../samples/fail/output_fail_01.txt
java Program ../samples/fail_02.minc   > ../samples/fail/output_fail_02.txt
java Program ../samples/fail_03a.minc  > ../samples/fail/output_fail_03a.txt
java Program ../samples/fail_03b.minc  > ../samples/fail/output_fail_03b.txt
java Program ../samples/fail_04.minc   > ../samples/fail/output_fail_04.txt
java Program ../samples/fail_05a.minc  > ../samples/fail/output_fail_05a.txt
java Program ../samples/fail_05b.minc  > ../samples/fail/output_fail_05b.txt
java Program ../samples/fail_06.minc   > ../samples/fail/output_fail_06.txt
java Program ../samples/fail_07.minc   > ../samples/fail/output_fail_07.txt
java Program ../samples/fail_08a.minc  > ../samples/fail/output_fail_08a.txt
java Program ../samples/fail_08b.minc  > ../samples/fail/output_fail_08b.txt
java Program ../samples/fail_09.minc   > ../samples/fail/output_fail_09.txt
java Program ../samples/fail_10.minc   > ../samples/fail/output_fail_10.txt

java Program ../samples/succ_01.minc   > ../samples/success/output_succ_01.txt
java Program ../samples/succ_02.minc   > ../samples/success/output_succ_02.txt
java Program ../samples/succ_03.minc   > ../samples/success/output_succ_03.txt
java Program ../samples/succ_04.minc   > ../samples/success/output_succ_04.txt
java Program ../samples/succ_05.minc   > ../samples/success/output_succ_05.txt
java Program ../samples/succ_06.minc   > ../samples/success/output_succ_06.txt
java Program ../samples/succ_07.minc   > ../samples/success/output_succ_07.txt
java Program ../samples/succ_08.minc   > ../samples/success/output_succ_08.txt
java Program ../samples/succ_09.minc   > ../samples/success/output_succ_09.txt
java Program ../samples/succ_10.minc   > ../samples/success/output_succ_10.txt
PAUSE
