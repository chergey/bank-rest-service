package org.elcer.accounts.resource;

import org.elcer.accounts.app.AppConfig;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.List;

class PagedResourceSupport {

    private UriBuilder startBuilder;

    PagedResourceSupport(UriBuilder startBuilder) {
        this.startBuilder = startBuilder;
    }

    Link getAllAccountsLink() {
        UriBuilder uriBuilder = startBuilder
                .path(AccountResource.class, "getAllAccounts").queryParam(AppConfig.PAGE_PARAM_NAME, 0)
                .queryParam(AppConfig.SIZE_PARAM_NAME, AppConfig.DEFAULT_PAGESIZE);

        Link.Builder linkBuilder = Link.fromUriBuilder(uriBuilder);
        return linkBuilder.rel("accounts")
                .title("All accounts")
                .build();
    }

    List<Link> createLinks(int page, int size, long total) {
        var links = new ArrayList<Link>();

        UriBuilder uriBuilder = startBuilder.queryParam(AppConfig.PAGE_PARAM_NAME, page)
                .queryParam(AppConfig.SIZE_PARAM_NAME, size);
        links.add(Link.fromUriBuilder(uriBuilder).rel(Page.SELF.toString()).title(Page.SELF.title()).build());

        int nextPage = (page + 1) * size;

        if (nextPage < total) {
            uriBuilder = startBuilder.queryParam(AppConfig.PAGE_PARAM_NAME, page + 1)
                    .queryParam(AppConfig.SIZE_PARAM_NAME, size);
            links.add(Link.fromUriBuilder(uriBuilder).rel(Page.NEXT.toString()).title(Page.NEXT.title()).build());
        }

        uriBuilder = startBuilder.queryParam(AppConfig.PAGE_PARAM_NAME, total / size)
                .queryParam(AppConfig.SIZE_PARAM_NAME, size);
        links.add(Link.fromUriBuilder(uriBuilder).rel(Page.LAST.toString()).title(Page.LAST.title()).build());


        if (page > 0) {
            uriBuilder = startBuilder.queryParam(AppConfig.PAGE_PARAM_NAME, page - 1)
                    .queryParam(AppConfig.SIZE_PARAM_NAME, size);
            links.add(Link.fromUriBuilder(uriBuilder).rel(Page.PREV.toString()).title(Page.PREV.title()).build());
        }
        return links;
    }

}
