/* jshint esversion: 11 */
/*
<a href="https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/create/866?customfield_10300=https%3A%2F%2Fsuche.thulb.uni-jena.de%2FRecord%2F1510173110&amp;customfield_13535=Zur+wirtschaftlichen+Lage&amp;summary=Zugangsproblem+%C3%BCber+ThULB-Suche" class="broken-link" title="Zugangsproblem?" target="_blank">
  <span class="broken-link-description">Zugangsproblem?</span><i class="ml-1 broken-link-icon"></i>
</a>

<button type="button" aria-label="D" class="button__rX4Lp button--icon__ZW5xS screen__chat-button__9Z7rf"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><g fill="currentColor"><path d="M21.628 11.093c0-3.261-4.004-5.905-8.943-5.905S3.742 7.83 3.743 11.093c0 1.421.76 2.725 2.027 3.744.218 2.124-.8 3.414-1.488 4.178 2.113.552 4.288-1.158 5.277-2.388.973.24 2.026.372 3.126.372 4.939 0 8.943-2.645 8.943-5.906zM3.803 20.99l-3.226-.844 2.272-2.523c.579-.642.895-1.179.972-1.849-1.295-1.31-2.04-2.926-2.04-4.68 0-4.63 5.017-7.944 10.904-7.944S23.59 6.464 23.59 11.093c0 4.63-5.017 7.943-10.904 7.943-.83 0-1.65-.066-2.445-.196-1.76 1.712-4.137 2.751-6.437 2.15z"></path><g transform="translate(7.611 9.674)"><ellipse cx="5.191" cy="1.354" rx="1.298" ry="1.354"></ellipse><ellipse cx="9.084" cy="1.354" rx="1.298" ry="1.354"></ellipse><ellipse cx="1.298" cy="1.354" rx="1.298" ry="1.354"></ellipse></g></g></svg></button>
 */
const dbtServiceDesk = (function () {
    'use strict';
    const baseUrl = "https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/create/847";

    const app = Object.freeze({
        "Collections": 16988,
        "Bibliography": 16989,
        "DBT": 16990,
        "JPortal": 16991,
        "Typo3": 16992,
        "Other": 16992
    });
    const userGroup = Object.freeze({
        "Extern": 16982,
        "ThULB": 16983,
        "FSU": 16984
    });
    const ticketType = Object.freeze({
        "General": 16985,
        "Bug": 16961,
        "Feature": 16986,
        "Other": 16963,
        "Account": 16987
    });

    class ServiceDeskForm {

        constructor(referrer, app, type, userGroup) {
            this.sdApp = app;
            this.sdType = type;
            this.sdUserGroup = userGroup;
            this.sdReferrer = referrer;
        }

        toParam() {
            return {
                "customfield_10300": this.sdReferrer,
                "customfield_11920": this.sdApp,
                "customfield_12342": this.sdType,
                "customfield_13206": this.sdUserGroup
            };
        }

        toUrl() {
            return `${baseUrl}?${Object.entries(this.toParam())
                .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
                .join("&")}`;
        }
    }

    return {
        App: app,
        UserGroup: userGroup,
        TicketType: ticketType,
        Form: ServiceDeskForm
    };
})();
