package org.elcer.accounts

import org.elcer.accounts.hk2.RequiredParamResourceFilterFactory
import org.elcer.accounts.hk2.annotations.Required
import org.glassfish.jersey.internal.util.collection.ImmutableMultivaluedMap
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

import javax.ws.rs.PathParam
import javax.ws.rs.QueryParam
import javax.ws.rs.WebApplicationException
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.FeatureContext
import javax.ws.rs.core.UriInfo

@ExtendWith(MockitoExtension)
class RequiredParamResourceFilterFactoryTest {

    void methodWithRequiredAnnoAndTwoParamAnnotations(@Required @PathParam("param") @QueryParam("param") String param) {
    }

    void methodWithRequiredAnnoAndNoParamAnnotations(@Required String param) {
    }

    void normalMethod(@Required @QueryParam("param") String param) {
    }


    @Mock
    private FeatureContext featureContext

    @Test
    void "test only one parameter annotation is allowed"() {
        def factory = new RequiredParamResourceFilterFactory()

        def resourceInfo = Mockito.mock(ResourceInfo)
        //   def featureContext= Mockito.mock(FeatureContext)

        def method = getClass().getDeclaredMethod("methodWithRequiredAnnoAndTwoParamAnnotations", String)
        Mockito.when(resourceInfo.getResourceMethod()).thenReturn(method)

        def thrown = Assertions.assertThrows(RuntimeException, () -> factory.configure(resourceInfo, featureContext))
        Assertions.assertTrue(thrown.getMessage().contains("Both PathParam and QueryParam are defined. Choose only one!"))

    }


    @Test
    void "test no parameter annotation is defined"() {
        def method = getClass().getDeclaredMethod("methodWithRequiredAnnoAndNoParamAnnotations", String)
        def filter = new RequiredParamResourceFilterFactory.RequiredParamFilter(method)

        def containerRequest = Mockito.mock(ContainerRequestContext)
        def uriInfo = Mockito.mock(UriInfo)
        Mockito.when(containerRequest.getUriInfo()).thenReturn(uriInfo)
        Mockito.when(uriInfo.getQueryParameters()).thenReturn(ImmutableMultivaluedMap.empty())

        def thrown = Assertions.assertThrows(RuntimeException, () -> filter.filter(containerRequest))
        Assertions.assertTrue(thrown.getMessage().contains("No @PathParam or @QueryParam defined!"))

    }

    @Test
    void "test no required parameter is supplied"() {
        def method = getClass().getDeclaredMethod("normalMethod", String)
        def filter = new RequiredParamResourceFilterFactory.RequiredParamFilter(method)

        def containerRequest = Mockito.mock(ContainerRequestContext)
        def uriInfo = Mockito.mock(UriInfo)
        Mockito.when(containerRequest.getUriInfo()).thenReturn(uriInfo)
        Mockito.when(uriInfo.getQueryParameters()).thenReturn(ImmutableMultivaluedMap.empty())

        def thrown = Assertions.assertThrows(WebApplicationException, () -> filter.filter(containerRequest))
                as WebApplicationException

        def message = thrown.getResponse().getEntity() as String
        Assertions.assertTrue(message.contains("Parameters are missing: param"))

    }


}
