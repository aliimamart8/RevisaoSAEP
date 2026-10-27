@echo off
chcp 65001 >nul
echo SAEP - Controle de Estoque de Autopecas
echo Verifique antes se o MySQL esta iniciado e o banco saep_db foi criado.
mvn clean compile exec:java -Dexec.mainClass="br.com.saep.App"
pause
