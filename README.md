
# ctc-presentation-notification-frontend

This service allows a user to check they have provided all the information necessary to get their goods released for transit.

Service manager port: 10134

### Testing

Run unit tests:
<pre>sbt test</pre>  
Run integration tests:
<pre>sbt it/test</pre>
Run accessibility linter tests:
<pre>sbt A11y/test</pre>

### Running manually or for journey tests

<pre>
sm2 --start CTC_TRADERS_P5_ACCEPTANCE
sm2 --stop CTC_PRESENTATION_NOTIFICATION_FRONTEND
sbt run
</pre>

We then need to post an IE015 with the `<additionalDeclarationType>` field set to `D` (pre-lodge), followed by a post of either a IE004 (Amendment Acceptance), IE928 (Positive Acknowledge) or IE060 (Control Decision Notification).

From the `/view-departure-declarations` page click the `Complete declaration` link for the relevant movement.

### Feature toggles

The following features can be toggled in [application.conf](conf/application.conf):

| Key                        | Argument type | sbt                                                            | Description                                                                                                                                                                                    |
|----------------------------|---------------|----------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `trader-test.enabled`      | `Boolean`     | `sbt -Dtrader-test.enabled=true run`                           | If enabled, this will override the behaviour of the "Is this page not working properly?" and "feedback" links. This is so we can receive feedback in the absence of Deskpro in `externaltest`. |
| `banners.showUserResearch` | `Boolean`     | `sbt -Dbanners.showUserResearch=true run`                      | Controls whether or not we show the user research banner.                                                                                                                                      |
| `play.http.router`         | `String`      | `sbt -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes run`  | Controls which router is used for the application, either `prod.Routes` or `testOnlyDoNotUseInAppConf.Routes`                                                                                  |


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").