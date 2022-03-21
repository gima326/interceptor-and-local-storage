### Re-frame の Interceptor と local storage 事始め

頭からドボンと飛び込むシリーズ。<br>
reagent、Re-frame…、その包含関係がまだよく分からないままですが。<br>
TODOリストを作成し Re-frame の使い方を確認する、という先達の例をベースに、いろいろいじって理解する。<br>

Step0: プロジェクト作成（インターセプター、ローカルストレージ利用）<br>
Step1: インターセプター内で、Spec による「状態」のバリデーション追加<br>

Step2: （値が不正だった場合、例外を投げて処理を終了させる、という）「Step1」の方針が気に入らない。<br>
　　　　エラーメッセージを表示して、再実行を促すにはどうしたらいいのかな、と試行錯誤。<br>


![list](https://github.com/gima326/interceptor-and-local-storage/blob/main/readme_img/list_img1.png)

![list](https://github.com/gima326/interceptor-and-local-storage/blob/main/readme_img/list_img2.png)

## References

- 「[Re−frame 入門][1]」<br>
[ `https://qiita.com/snufkon/items/1d409c984faaa3c390a1` ]<br>
- 「[TodoMVC done with re-frame][2]」<br>
[ `https://github.com/Day8/re-frame/tree/master/examples/todomvc` ]<br>

[1]: https://qiita.com/snufkon/items/1d409c984faaa3c390a1
[2]: https://github.com/Day8/re-frame/tree/master/examples/todomvc
