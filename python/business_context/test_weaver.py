import unittest

from typing import Set
from business_context.context import BusinessContext
from business_context.expression import Constant
from business_context.identifier import Identifier
from business_context.operation_context import OperationContext
from business_context.registry import LocalBusinessContextLoader, RemoteLoader, Registry, RemoteBusinessContextLoader
from business_context.rule import Precondition, PostCondition, PostConditionType
from business_context.weaver import Weaver, BusinessRulesCheckFailed


class WeaverTest(unittest.TestCase):

    def test_evaluate_preconditions(self):
        auth_logged_in_identifier = Identifier("auth", "userLoggedIn")
        user_create_identifier = Identifier("user", "create")

        precondition_1 = Precondition('precondition_1', Constant(True))
        precondition_2 = Precondition('precondition_2', Constant(False))

        post_condition_1 = PostCondition('post_condition_1', 'user', PostConditionType.FILTER_OBJECT_FIELD, Constant(True))
        post_condition_2 = PostCondition('post_condition_2', 'user', PostConditionType.FILTER_OBJECT_FIELD, Constant(True))

        auth_logged_in = BusinessContext(auth_logged_in_identifier, set(), {precondition_1}, {post_condition_1})
        user_create = BusinessContext(user_create_identifier, {auth_logged_in_identifier}, {precondition_2}, {post_condition_2})

        class MockLocalLoader(LocalBusinessContextLoader):

            def load(self) -> Set[BusinessContext]:
                return {user_create}

        class MockRemoteLoader(RemoteLoader):
            _contexts = {auth_logged_in_identifier: auth_logged_in}

            def load_contexts(self, identifiers: Set[Identifier]) -> Set[BusinessContext]:
                result = set()
                for identifier in identifiers:
                    if identifier in self._contexts:
                        result.add(self._contexts[identifier])
                return result

        registry = Registry(MockLocalLoader(), RemoteBusinessContextLoader(MockRemoteLoader()))
        weaver = Weaver(registry)
        operation_context = OperationContext('user.create')
        self.assertRaises(BusinessRulesCheckFailed, weaver.evaluate_preconditions, operation_context)
