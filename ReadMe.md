
Create a REST application with a single endpoint :


`POST /api/convert`
Body:
```
{
    "fromCurrency": "GBP",
    "toCurrency" : "EUR",
    "amount" : 102.6
}
```
The return should be an object with the exchange rate between the `fromCurrency` to
`toCurrency` and the amount converted to the second currency.
```
{
    "exchange" : 1.11,
    "amount" : 113.886,
    "original" : 102.6
}
```

## Running

Run this using [sbt](http://www.scala-sbt.org/).  If you downloaded this project from repository then you'll find a prepackaged version of sbt in the project directory:

```bash
sbt run
```

And then go to <http://localhost:9000> to see the running web application.

## Test
```bash
sbt test
```

## Improvements

- Dockerization
- More tests
- Avoid exceptions
- Input validation
- Monitoring and limiting
- Authentication
