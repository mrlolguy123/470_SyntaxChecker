del *.class
java -jar jflex-1.6.1.jar Lexer.flex
javac *.java

SET "samplesPath=..\samples"

FOR /R %samplesPath% %%G IN (*.minc) DO (
	java Program "%%G" > "%%~dpGoutput_%%~nG.txt"
)

PAUSE