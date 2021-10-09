# cljs-r3f-example
Some react three fiber demos in clojurescript. 
You can see live pages [here](https://itarck.github.io/cljs-r3f-example/).

Some demos are copied from [binaryage](https://github.com/binaryage/cljs-react-three-fiber), but simplified.

Tools used in repo:
* threejs: [react-three-fiber], [drei]
* react: [helix][], [reagent][]
* js interop: [js-interop][], [cljs-bean][]
* state management: [ratom], [datascript], [posh]

[react-three-fiber]: https://github.com/pmndrs/react-three-fiber
[drei]: https://github.com/pmndrs/drei
[helix]: https://github.com/lilactown/helix
[reagent]: https://github.com/reagent-project/reagent
[ratom]: https://github.com/reagent-project/reagent
[js-interop]: https://github.com/applied-science/js-interop
[cljs-bean]: https://github.com/mfikes/cljs-bean
[datascript]: https://github.com/tonsky/datascript
[posh]: https://github.com/denistakeda/posh

## Development mode
```
yarn
npx shadow-cljs watch app
```

then, visit [http://localhost:3000](http://localhost:3000)

