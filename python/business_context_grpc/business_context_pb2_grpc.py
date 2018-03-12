# Generated by the gRPC Python protocol compiler plugin. DO NOT EDIT!
import grpc

from business_context_grpc import business_context_pb2 as business__context__pb2


class BusinessContextServerStub(object):
  # missing associated documentation comment in .proto file
  pass

  def __init__(self, channel):
    """Constructor.

    Args:
      channel: A grpc.Channel.
    """
    self.FetchContexts = channel.unary_unary(
        '/businessContext.BusinessContextServer/FetchContexts',
        request_serializer=business__context__pb2.BusinessContextRequestMessage.SerializeToString,
        response_deserializer=business__context__pb2.BusinessContextsResponseMessage.FromString,
        )


class BusinessContextServerServicer(object):
  # missing associated documentation comment in .proto file
  pass

  def FetchContexts(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')


def add_BusinessContextServerServicer_to_server(servicer, server):
  rpc_method_handlers = {
      'FetchContexts': grpc.unary_unary_rpc_method_handler(
          servicer.FetchContexts,
          request_deserializer=business__context__pb2.BusinessContextRequestMessage.FromString,
          response_serializer=business__context__pb2.BusinessContextsResponseMessage.SerializeToString,
      ),
  }
  generic_handler = grpc.method_handlers_generic_handler(
      'businessContext.BusinessContextServer', rpc_method_handlers)
  server.add_generic_rpc_handlers((generic_handler,))
