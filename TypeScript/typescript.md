# ReactとTypeScript
### ParcelによるReact環境開発

### create-react-app によるReact環境開発
- Reactのインストール
#### npmの場合
```
npm install -g create-react-app
```

#### yarnの場合
```
yarn add create-react-app
```

#### npxの場合
```
npx create-react-app new-app-name --typescript
```

npmまたはyarnによるインストールが完了したら、次のコマンドを実行する
```
create-react-app new-app-name --typescript
```
`--typescript`オプションを付与することでReact×TypeScriptのアプリの雛形が作成される。<br>
※`new-app-name`はプロジェクトの名称<br><br>


アプリケーションは`yarn start`で起動する。<br>
起動したアプリケーションは`http://localhost:3000/`にアクセスすると閲覧できる。<br><br>

JavaScriptの場合はエントリーポイントとなるコンポーネントファイルは`index.jsx`だが、TypeScriptを利用している場合は、拡張子が`tsx`となる。

### データ型の定義
Webアプリケーションのフロントエンドの実装では、APIでデータを取得し、それを表示する作業が多くなる。

- API等で取得したデータを疑似的に表現した`data.ts`
```typescript:data.ts
export const rows = [
    {
        id: 'up20-un30',
        generation: '20～30歳',
        answers: [0.18, 0.22, 0.37, 0.23]
    },
    {
        id: 'up30-un40',
        generation: '30～40歳',
        answers: [0.12, 0.28, 0.42, 0.18]
    }
]

// 定義したdataに型を定義する
export type Row = {
    id: string
    generation: string
    answers: number[]
}

export type Rows = Row[]
```

# Vue.jsとTypeScript

# ExpressとTypeScript

# Next.jsとTypeScript

# Nuxt.jsとTypeScript
