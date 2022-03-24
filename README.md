### Re-frame の Interceptor と local storage 事始め

Step0: プロジェクト作成（インターセプター、ローカルストレージ利用）<br>
Step1: インターセプター内で、Spec による「状態」のバリデーション<br>
Step2: 不正な値にたいしてエラーメッセージを表示する。<br>

「Step1」では、Interceptor 関数にて、値を Spec チェックをして、妥当でない場合には例外を投げる、ということをしている。<br>

　・初期処理<br>
　　ローカルストレージに保持している値（既存レコード）が妥当でない場合、例外を投げる。<br>
  　レコード表示が行われない。その原因も伝えられない。<br>

　・ユーザー入力値のチェック処理<br>
　　妥当でない場合、例外を投げる。<br>
  　入力値の表示が行われない。また、その原因もユーザーに伝えられない。<br>

これ、参考にしたサイト「TodoMVC done with re-frame」の例を写経したものなんだけど、<br>
どうも効果的な使い方になっていないな、と（まぁ、説明のための例ですからね）。<br>



![list](https://github.com/gima326/interceptor-and-local-storage/blob/main/readme_img/list_img1.png)

![list](https://github.com/gima326/interceptor-and-local-storage/blob/main/readme_img/list_img2.png)

## References

- 「[Re−frame 入門][1]」<br>
[ `https://qiita.com/snufkon/items/1d409c984faaa3c390a1` ]<br>
- 「[TodoMVC done with re-frame][2]」<br>
[ `https://github.com/Day8/re-frame/tree/master/examples/todomvc` ]<br>

[1]: https://qiita.com/snufkon/items/1d409c984faaa3c390a1
[2]: https://github.com/Day8/re-frame/tree/master/examples/todomvc
