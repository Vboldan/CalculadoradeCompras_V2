# 🛒 Calculadora de Compras

Um aplicativo Android nativo desenvolvido em Java para auxílio e controle de orçamentos durante compras em tempo real. O app permite gerenciar o limite disponível, adicionar e remover itens dinamicamente e gerar um relatório completo da compra em formato PDF.

🚀 Funcionalidades
Controle de Saldo em Tempo Real: Define o limite disponível e calcula automaticamente o total gasto e o saldo restante a cada item adicionado.

Gestão de Produtos: Adição de itens informando nome, preço unitário e quantidade, com suporte a seleção visual para exclusão.

Navegação de Teclado Otimizada: Fluxo contínuo entre os campos para preenchimento rápido.

Geração de PDF: Exportação de nota de compra estilizada com data, tabela e destaque do produto mais caro.

Compartilhamento Direto: Envio do arquivo PDF via apps de mensagens ou e-mail (FileProvider).

Interface Dark Mode: Design escuro moderno (#23272D) ajustado para conforto visual.

🛠️ Tecnologias Utilizadas
Linguagem: Java

UI / Layout: XML (Material Design 3, ScrollView, LinearLayout)

Manipulação de PDF: android.graphics.pdf.PdfDocument e Canvas

Compartilhamento de Arquivos: AndroidX FileProvider

📂 Estrutura do Projeto

app/src/main/
├── java/com/exemplo/calculadoradecompras/
│   ├── MainActivity.java
│   └── Produto.java
└── res/
    ├── layout/
    │   └── activity_main.xml
    ├── xml/
    │   └── filepaths.xml
    └── values/
        └── themes.xml

🔧 Como Executar o Projeto

Clone o repositório:

```bash
git clone https://github.com/Vboldan/CalculadoradeCompras_V2.git
cd CalculadoradeCompras_V2
```

Abra a pasta do projeto no Android Studio ou Visual Studio Code. Use o JDK 17 e instale a plataforma Android SDK 35.

Para gerar o APK de depuração pela linha de comando:

```bash
./gradlew assembleDebug
```

O arquivo será gerado em `app/build/outputs/apk/debug/app-debug.apk`.

Para instalar e executar o aplicativo, conecte um dispositivo físico com Depuração USB ativada ou inicie um emulador Android.

📝 Licença
Este projeto está sob a licença MIT.
