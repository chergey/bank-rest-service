package org.elcer.accounts.resource;

import org.elcer.accounts.app.AppConfig;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

class PagedResourceSupport {
    private static final Method GET_ALL_ACCOUNTS_METHOD;


    static {
        try {
            GET_ALL_ACCOUNTS_METHOD = AccountResource.class.getMethod("getAllAccounts", int.class, int.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

    }

    private UriBuilder startBuilder;

    PagedResourceSupport(UriBuilder startBuilder) {
        this.startBuilder = startBuilder;
    }

    Link getAllAccountsLink() {
        UriBuilder uriBuilder = startBuilder
                .path(GET_ALL_ACCOUNTS_METHOD).queryParam(AppConfig.PAGE_PARAM_NAME, 0)
                .queryParam(AppConfig.SIZE_PARAM_NAME, AppConfig.DEFAULT_PAGESIZE);

        Link.Builder linkBuilder = Link.fromUriBuilder(uriBuilder);
        return linkBuilder.rel("accounts").build();
    }

    List<Link> createLinks(int page, int size, long total) {
        var links = new ArrayList<Link>();

        UriBuilder uriBuilder = startBuilder.queryParam(AppConfig.PAGE_PARAM_NAME, page)
                .queryParam(AppConfig.SIZE_PARAM_NAME, size);
        links.add(Link.fromUriBuilder(uriBuilder).rel("self").build());

        int requestedElements = (size + 1) * page;

        if (requestedElements < total) {
            uriBuilder = startBuilder.queryParam(AppConfig.PAGE_PARAM_NAME, page + 1)
                    .queryParam(AppConfig.SIZE_PARAM_NAME, size);
            links.add(Link.fromUriBuilder(uriBuilder).rel("next").build());
        }

        uriBuilder = startBuilder.queryParam(AppConfig.PAGE_PARAM_NAME, total / size)
                .queryParam(AppConfig.SIZE_PARAM_NAME, size);
        links.add(Link.fromUriBuilder(uriBuilder).rel("last").build());


        if (page > 1) {
            uriBuilder = startBuilder.queryParam(AppConfig.PAGE_PARAM_NAME, page - 1)
                    .queryParam(AppConfig.SIZE_PARAM_NAME, size);
            links.add(Link.fromUriBuilder(uriBuilder).rel("prev").build());
        }
        return links;
    }

}
