Spring Boot(1系) + spring-security-oauth2 + Redis Session + Heroku で SerializationException 

Spring Boot(1系) + spring-security-oauth2 + Redis Session の組み合わせで Heroku上で動かすと SerializationException が発生した。
(1インスタンスなら起きない。複数インスタンスの場合のみ)

## 原因と解決策

ちなみにSpring Boot 2系(Spring5系) を使っている場合は Spring Security5系 を使用すれば良いらしい。(試してない)
Spring Boot 2系(Spring5系)に上げられるならその方がいい

問題はどうやら spring-security-oauth2 にあるっぽい。
ググって出てきた解決策は SerializationException が起きたらRedisから消すという方法。([id:katsu68 さんのブログ](http://katsu-tech.hatenablog.com/entry/2017/10/18/220901))
ただし、Heroku上だと頻繁にSerializationExceptionが起きるため、ログイン状態をほとんど維持できないことになる。
[Heroku スティッキーセッションの設定](https://devcenter.heroku.com/articles/session-affinity)をすればとりあえず動くが、再起動が走るとやはりSerializationExceptionが起きる。

そのため両方の対応を行い対応した。
デプロイ時とHerokuのインスタンス再起動時だけセッション切れするがしばらくはこれで運用することに。

しかし、Herokuの再起動はいつされるかわからない(任意のタイミングで再起動することによりある程度はコントロールできるが)
頻繁にログアウトされるのはユーザにとってもストレス。

## ライブラリを作った

それを回避するため一部処理を自分で作ることにした。
また、それをライブラリとして公開することにもした。 -> [作ったライブラリ](https://github.com/ap-com/spring-oauth2-serializable)

```xml
		<dependency>
			<groupId>jp.co.ap-com</groupId>
			<artifactId>spring-oauth2-serializable</artifactId>
			<version>0.0.1</version>
		</dependency>
```

spring-security-oauth2 の設定が済んでいるならばソースコードは`@EnableOAuth2Sso` を `@EnableOAuth2Serializable` に変えるだけ
一箇所でも `@EnableOAuth2Sso` か `@EnableOAuth2Client` が残ってるとダメ。

デモ用アプリケーションはこちら
https://github.com/apc-hattori/spring-oauth2-demo

デモ用アプリケーションの解説

ブランチ use-spring-security-oauth2 では spring-security-oauth2 を使ったもの
起動し、何度かログインを試すとSerializationExceptionが発生するはず
ここから下記リンクのように修正する
https://github.com/apc-hattori/spring-oauth2-demo/compare/use-spring-security-oauth2...master
use-spring-security-oauth2の状態で動かした場合は念の為redisをflushallした方がいいかも
```
docker-compose exec redis redis-cli flushall
```
ブランチ master の状態だと SerializationException が起きない 
