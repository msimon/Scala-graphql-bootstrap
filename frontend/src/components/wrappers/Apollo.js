import React from 'react';

import { ApolloProvider } from '@apollo/react-hooks';
import ApolloClient from 'apollo-boost';

import config from "config"

const apolloClient = new ApolloClient({
  uri: config.backendUrl,
  credentials: "include"
})

export default (ComposedComponent) => {
  return class ApolloWrapper extends React.Component {
    static displayName = `ApolloWrapper(${ComposedComponent.displayName})`

    render() {
      return (
        <ApolloProvider client={apolloClient}>
          <ComposedComponent {...this.props} apolloClient={apolloClient} />
        </ApolloProvider>
      )
    }
  }
}
