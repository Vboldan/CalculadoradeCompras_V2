Markdown
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
Plaintext
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

Bash
git clone https://github.com/Vboldan/CalculadoradeCompras_V2.git
Abra o projeto:

Importe a pasta no Android Studio ou Visual Studio Code.

Compile e Execute:

Conecte um dispositivo físico via Depuração USB ou inicie um Emulador Android.

📝 Licença
Este projeto está sob a licença MIT.

Após salvar, execute no terminal do VS Code para atualizar no GitHub: