
# ctc-presentation-notification-frontend

This service allows a user to check they have provided all the information necessary to get their goods released for transit.

Service manager port: 10134

### Testing

Run unit tests:
<pre>sbt test</pre> 

### Running manually or for journey tests

    sm --start CTC_TRADERS_P5_ACCEPTANCE -r
    sm --stop "CTC_PRESENTATION_NOTIFICATION_FRONTEND"
    sbt run

We then need to post an IE015 with the `<additionalDeclarationType>` field set to `D` (pre-lodge), followed by a post of either a IE004 (Amendment Acceptance), IE928 (Positive Acknowledge) or IE060 (Control Decision Notification).

From the `/view-departure-declarations` page click the `Complete declaration` link for the relevant movement.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").