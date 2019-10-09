import React from 'react';

import _ from 'lodash'
import gql from 'graphql-tag';
import { useQuery, useMutation } from '@apollo/react-hooks';
import { Form, Field } from 'react-final-form'
import {useCookies} from 'react-cookie'

import ApolloWrapper from 'components/wrappers/Apollo'
import config from "config"

import './App.css';

const getUserQuery = gql`
  query getUser {
    user {
      id
      email
    }
  }
`

const createUserMutation = gql`
  mutation createUser($email: String!, $password: String!) {
    createUser(
      email: $email
      password: $password
    ) {
      id
      email
      token {
        id
        token
        expiresAt
      }
    }
  }
`

function RenderCreateUser(props) {
  const [createUser, createUserState] = useMutation(createUserMutation) // TODO handle error status

  const onSubmit = (variables) => {
    createUser({
      variables: variables
    }).then(({data}) => {
      props.onSuccess(data.createUser)
    }).catch((error) => {
      console.log("Error", error)
    })
  }

  const errorHtml = createUserState.loading === false && createUserState.error ?
    <p>{createUserState.error.toString()}</p> : <></>

  return <div>
    {errorHtml}
    <Form
      onSubmit={onSubmit}
      // validate={validate}
      render={({ handleSubmit }) => (
        <form onSubmit={handleSubmit}>
          <Field name="email" component="input" placeholder="Email" />
          <Field name="password" component="input" type="password" placeholder="Password" />
          {createUserState.loading ? <></> : <button type="submit">create</button>}
        </form>
      )}
    />
  </div>
}


function App() {
  const getUser = useQuery(getUserQuery)
  const setTokenCookie = useCookies(['token'])[1];

  const onUserCreation = (createUserData) => {
    const tokenObj = createUserData.token
    setTokenCookie(
      "token",
      tokenObj.token,
      {
        ...config.cookies,
        path: "/",
        sameSite: true,
        expires: new Date(tokenObj.expiresAt),
      }
    )
    getUser.refetch()
  }

  if (getUser.loading) {
    return <div>loading</div>
  } else if (getUser.error) {
    if (_.get(getUser.error, 'graphQLErrors.0.key') === "not_authorized") {
      return (
        <div className="App">
          <header className="App-header"><RenderCreateUser onSuccess={onUserCreation} />
          </header>
        </div>
      )
    } else {
      return <div>Error: {getUser.error.toString()}</div>
    }
  } else {
    return (
      <div className="App">
        <header className="App-header">
          <p>
            hello {getUser.data.user.email}
          </p>
        </header>
      </div>
    );
  }
}

export default ApolloWrapper(App);
