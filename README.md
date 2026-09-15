# 🛒 Calculadora de Compras

Um aplicativo Android nativo desenvolvido em Java para auxílio e controle de orçamentos durante compras em tempo real. O app permite gerenciar o limite disponível, adicionar e remover itens dinamicamente e gerar um relatório completo da compra em formato PDF.

🚀 Funcionalidades
Controle de Saldo em Tempo Real: Define o limite disponível e calcula automaticamente o total gasto e o saldo restante a cada item adicionado.

Gestão de Produtos: Adição de itens informando nome, preço unitário e quantidade, com suporte a seleção visual para exclusão.

Navegação de Teclado Otimizada: Fluxo contínuo entre os campos via tecla Avançar (actionNext) para facilitar o preenchimento rápido.

Geração de PDF: Exportação de nota de compra estilizada com:

Cabeçalho com data e hora.

Tabela organizada com produto, preço, quantidade e subtotal.

Resumo financeiro (limite, total gasto, saldo restante).

Destaque automático para o produto mais caro da lista.

Compartilhamento Direto: Envio do arquivo PDF gerado via apps de mensagens ou e-mail (FileProvider).

Interface Dark Mode: Design escuro moderno (#23272D) ajustado para economia de bateria e conforto visual.

🛠️ Tecnologias Utilizadas
Linguagem: Java

UI / Layout: XML (Material Design 3, ScrollView, LinearLayout, MaterialButton)

Manipulação de PDF: android.graphics.pdf.PdfDocument e Canvas

Compartilhamento de Arquivos: AndroidX FileProvider



📂 Estrutura do Projeto


app/src/main/
├── java/com/exemplo/calculadoradecompras/
│   ├── MainActivity.java    # Lógica da interface, cálculos, eventos e PDF
│   └── Produto.java         # Modelo de dados do produto
└── res/
    ├── layout/
    │   └── activity_main.xml # Layout da tela principal
    ├── xml/
    │   └── filepaths.xml     # Configuração de caminhos do FileProvider
    └── values/
        └── themes.xml        # Configurações do tema do app


🔧 Como Executar o Projeto
Clone o repositório:

git clone [https://github.com/Vboldan/CalculadoradeCompras_V2.git](https://github.com/Vboldan/CalculadoradeCompras_V2.git)

Abra o projeto:

Importe a pasta no Android Studio ou Visual Studio Code (com suporte a Android/Java).

Compile e Execute:

Conecte um dispositivo físico via Depuração USB ou inicie um Emulador Android (API 24 ou superior).

Execute a build do projeto via Gradle.

📝 Licença
Este projeto está sob a licença MIT. Sinta-se à vontade para utilizar, modificar e contribuir!