del *.class
java -jar jflex-1.6.1.jar Lexer.flex
javac *.java

java Program ../samples/fail_01.minc   > ../samples/output_fail_01.txt

java Program ../samples/succ_01.minc   > ../samples/output_succ_01.txt

PAUSE
