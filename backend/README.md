# GraphQl Scala Backend

## How to build & Start
`sbt run`

It will take a while the first time to download all the packages

The server will run on port 8080


## Test the server
Some basic Query & Mutation have been defined. Take a look at the test /src/test/scala/Schema.scala

The easiest to test without a server is to use graphiQL: https://github.com/skevy/graphiql-app/releases
Once the server is running enter `http://0.0.0.0:8080/graphql` as the query endpoint

You can add some value with:
```
mutation addToken {
  addToken(value: "u832u9efuwj", provider: "slack") {
    id
    value
    provider
  }
}
```
and query them with:

```
query getTokens {
  tokens {
    id
    provider
    value
  }
}
```
or
```
query getToken {
  token(id: "$ID") {
    id
    provider
    value
  }
}
```
