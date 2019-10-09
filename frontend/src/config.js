const configDict = {
    'local': require('configs/local').config
}

const possible_env = ['local']

if (possible_env.includes(process.env.REACT_APP_ENV) === false) {
    throw Error("REACT_APP_ENV must be set to one of this value: " + possible_env.join(','))
}

export default configDict[process.env.REACT_APP_ENV]
