
Example:

- Installer Java
- Installer Maven
- `mvn package`

Utilisation:

- `java -jar target/templatemailer-1.0-SNAPSHOT.jar  --source test.csv --template template.stg  --from crashub@foobar.com --host smtp.gmail.com --secure --port 587 --username crashub@foobar.com --password somepassword --subject "c est super génial"`