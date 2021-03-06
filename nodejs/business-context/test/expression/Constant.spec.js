"use strict";

let chai = require('chai');

chai.should();

import Constant from './../../src/expression/Constant'
import ExpressionType from './../../src/expression/ExpressionType'
import BusinessOperationContext from './../../src/weaver/BusinessOperationContext'

describe('Constant', () => {
    describe('#interpret', () => {
        it('returns the value of the Constant', () => {
            const context = new BusinessOperationContext('user.create')
            let expression = new Constant(true, ExpressionType.BOOL)
            let result = expression.interpret(context)
            result.should.equal(true)

            expression = new Constant('lorem ipsum', ExpressionType.STRING)
            result = expression.interpret(context)
            result.should.equal('lorem ipsum')
        });
    });
});
