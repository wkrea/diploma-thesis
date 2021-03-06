"use strict";

const client = require('./../dist/client')
const server = require('./../dist/server')

const chai = require('chai')

const BusinessContextRegistry = require('business-context-framework/dist/BusinessContextRegistry').default
const BusinessContext = require('business-context-framework/dist/BusinessContext').default
const BusinessContextIdentifier = require('business-context-framework/dist/BusinessContextIdentifier').default
const Precondition = require('business-context-framework/dist/Precondition').default
const PostCondition = require('business-context-framework/dist/PostCondition').default
const PostConditionType = require('business-context-framework/dist/PostConditionType').default
const IsNotNull = require('business-context-framework/dist/expression/IsNotNull').default
const ExpressionType = require('business-context-framework/dist/expression/ExpressionType').default
const VariableReference = require('business-context-framework/dist/expression/VariableReference').default
const Constant = require('business-context-framework/dist/expression/Constant').default

chai.should()

const port = 5552

describe('business context exchange', () => {
    it('works', () => {
        const registry = new BusinessContextRegistry(
            {
                load: () => {
                    return [
                        new BusinessContext(
                            BusinessContextIdentifier.of('user.validEmail'),
                            new Set(),
                            new Set().add(
                                new Precondition('emailNotNull', new IsNotNull(new VariableReference('email', ExpressionType.STRING))),
                            ),
                            new Set()
                        ),
                        new BusinessContext(
                            BusinessContextIdentifier.of('user.create'),
                            new Set().add(BusinessContextIdentifier.of('auth.loggedIn')).add(BusinessContextIdentifier.of('user.validEmail')),
                            new Set(),
                            new Set().add(
                                new PostCondition('hideEmail', PostConditionType.FILTER_OBJECT_FIELD, 'email', new Constant(true, ExpressionType.BOOL))
                            )
                        ),
                    ]
                }
            },
            {
                loadContextsByIdentifier: (identifiers) => {
                    return {
                        'auth.loggedIn': new BusinessContext(
                            BusinessContextIdentifier.of('auth.loggedIn'),
                            new Set(),
                            new Set(),
                            new Set()
                        )
                    }
                }
            }
        )
        const contextServer = server.serve(port, registry)
        setTimeout(
            () => {
                client.fetchContexts(['user.create'], '0.0.0.0', port)
                    .then(contexts => {
                        contexts.length.should.equal(1)
                        const context = contexts.pop()
                        context.preconditions.size.should.equal(1)
                        context.postConditions.size.should.equal(1)

                        const precondition = context.preconditions.values().next().value
                        precondition.name.should.equal('emailNotNull')
                        precondition.condition.argument.name.should.equal('email')
                        precondition.condition.argument.type.should.equal(ExpressionType.STRING)

                        const postCondition = context.postConditions.values().next().value
                        postCondition.name.should.equal('hideEmail')
                        postCondition.type.should.equal(PostConditionType.FILTER_OBJECT_FIELD)
                        postCondition.referenceName.should.equal('email')
                        postCondition.condition.value.should.equal(true)
                    })
            },
            500 // Wait 500ms before sending request so the server has time to start
        )
        setTimeout(
            () => {
                client.fetchAllContexts('0.0.0.0', port)
                    .then(contexts => {
                        contexts.length.should.equal(2)
                    })
                    .catch(err => console.log(err))
            },
            600 // Wait 600ms before sending request so the server has time to start
        )
        setTimeout(
            () => contextServer.tryShutdown((err) => {
                if (err) console.log(err)
            }),
            1100 // Wait 1000ms before shutting down the server
        )
    })
})
