# Informações importantes

# Passo a Passo para montar ambiente do Desenvolvedor

## Passo 1

### Gerando chave de acesso para download do Projeto
Caso ocorra o seguinte erro ao baixar o projeto 
```
fatal: unable to access 'https://gitcorporativo.serpro/siconv-42811/licitacoes/siconv-licitacoes-backend.git/': server certificate verification failed. CAfile: /etc/ssl/certs/ca-certificates.crt CRLfile: none
```
Será necessario gerar uma chave pública de acesso como mostra a seguir:
```
Gerar chave pública
ssh-keygen
<enter>
<enter>
<enter>
Executar o comando 
cat ~/.ssh/id_rsa.pub
Copiar char exibida no console.
Ex.: 
Começa com: ssh-rsa ...
```
Obter a chave e adicionar no gitcorporativo como mostrado abaixo:

Em https://gitcorporativo.serpro/

Acessar o profile

Selecione
* Edit
* SSH key

E adicione a chave copiada.

De volta ao console
```
Execute o git clone <do seu projeto>
```
## Passo 2

### Ambiente de Desenvolvimento
Instalar Java JDK `apartir da versão 11.
Instalar o Eclipse

### Backend 

[Vídeo montagem ambiente quarkus](https://serprogovbr.sharepoint.com/:v:/r/sites/DGTU3/Shared%20Documents/General/07-Grava%C3%A7%C3%B5es/ConfiguracaoQuarkus.mp4?csf=1&web=1)

Baixar o Projeto
Executar instalação do lombok.jar
```
java -jar lombok.jar
```
No instalador localizar a pasta de instalação do Eclipse

Em seguida...
* Importar como Maven o projeto baixado no Eclipse
* Executar o clean -> Project do Eclipse
* Executar o Maven install no projeto
* Criar atalho para execução com os argumentos
```
-Djavamelody.datasources=java:jboss/datasources/cps-ds -Dswarm.logging=INFO -Xmx512m -XX:MaxMetaspaceSize=256m -XX:ParallelGCThreads=4 -XX:NewRatio=2 -XX:SurvivorRatio=20 -XX:+UseGCOverheadLimit -XX:+ExitOnOutOfMemoryError -Djava.net.preferIPv4Stack=true -Djava.awt.headless=true -Dthorntail.project.stage=local
```
OK, Projeto rodando!


### Configurar Análise estática de código (SonarLint)

- Gerar o certificado do SonarQube
    - Acessar o site [Sonar](https://sonarqube.aic.serpro/)
    - No cadeado que aparece ao lado do endereço do navegador, Gerar e exportar o certificado com qualquer nome. Para o nosso exemplo será: "aic.serpro"
        * Na versão mais atual do firefox, ir em: Preferences -> Privacy Security -> View Certificates
            - Selecionar e Exportar certificado aic.serpro
            
    
- Importar na JVM, utilizada no Eclipse, o certificado gerado
    - Na pasta da JVM 
    `/jdk-11.0.4/lib/security`
    - Executar o comando
    - `keytool -import -alias <alias> -keystore cacerts -file <nome arquivo>`
    - Ex.: `keytool -import -alias aic.serpro -keystore cacerts -file aic.serpro`
    - Se solicitar senha: changeit
    - Reiniciar o eclipse


- Acessar o SonarQube
    - Menu do usuário 
        - My Account
        - Security
        - Gerar token

- No Eclipse
    - Baixar o Plugin do SonarLint 
    - Na view SonarLint clicar com o botão direito e selecionar "New Connection" para conectar com o Servidor do SonarQube
    - Informar a URL: https://sonarqube.aic.serpro
    - Para login informar o token gerado para o usuário.
    - Cadastrar os Projetos
    - Se pedir o Projectkey: 42811.SICONV.MEDICAO.BACKEND

Obs.: Ao instalar o plugin, caso ocorra o erro "lombok/launch/PatchFixesHider$ValPorta", realizar os seguintes passos:
    - Copiar o arquivo 'lombok-1.18.12.jar', que se encontra na pasta .m2/repository/org/projectlombok/lombok/1.18.12/
    - Colar este arquivo na pasta do eclipse, e alterar no arquivo de inicialização (eclipse.ini) para apontar para esta versão do lombok.

Obs2.: Caso apareça o erro "Unable execute request: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path" ao colocar o token no eclipse, verifique no eclipse.ini se o eclipse está sendo executado com a mesma JVM onde foi adicionado o certificado.

### Frontend
Baixar o Projeto

### Instalar o NVM (Node Version Manager)

```
wget -qO- https://raw.githubusercontent.com/nvm-sh/nvm/v0.35.0/install.sh | bash
```

### Instalar o Node e NPM através do NVM

```
sudo nvm install 10.19.0
```
Adicionar o repositorio para os projetos do Serpro
```
npm config set registry https://nexus.aic.serpro.gov.br/repository/npm-group/
```
Maiores informações clique [aqui](https://nono.ma/configure-npm-registry)


Na pasta do projeto Atualizar as dependências
```
npm i
```
caso ocorra o seguinte erro:
```
38880 error typeerror Error: Missing required argument #1
```
Execute
```
npm install latest-version
npm install -g @angular/cli
npm start
```
OK, Projeto rodando!


## Para executar o banco do Medição (Postgres) local no docker

Instalar o docker
```
sudo apt install docker.io
```
Em seguida, instalar o docker compose
```
sudo apt install docker-compose
```
Alterar a configuração local (i.e. stage local) do arquivo project-stages.yml (src/main/resources/) informando a url de conexão do banco local
```
project:
    stage: local
  ...
  datasources:
    data-sources:
        medicao:
        connection-url: jdbc:postgresql://localhost:5432/siconv_mandatarias_desenv
        
```
Abrir o diretório docker/siconv-medicao-db/ em um terminal e digitar o comando abaixo para rodar o banco Postgres no ambiente local
```
docker-compose up -d
```
Outros comandos importantes:
```
Ctrl+C no terminal -- Encerra a execução do banco
docker-compose up -- Inicia novamente o banco
docker-compose up -d -- Inicia novamente o banco em modo silencioso. O console pode ser fechado sem que o serviço seja interrompido.
docker-compose stop -- Para o banco. Executa um shutdown no banco preservando os dados.
docker-compose up -V -- Inicia o banco limpando os dados salvos de uma execução anterior
docker-compose down --volumes -- Para a instância do banco e remove os volumes criados. Na prática, apaga o banco.
docker-compose run -d -p 5432:5432 postgres -- Na pasta do docker do projeto. Para criar uma nova instância do banco. Ex.: Pode ser utilizado para criar um banco com a versão de produção, coexistindo mais de uma instância do banco do Medição. Desenv e Produção ao mesmo tempo.
```
Executar a aplicação siconv-medicao-backend.

OK!


## Links úteis
* [Padrão Visual SICONV] (https://git.serpro/02223110452/Siconv/wikis/Novo-Padr%C3%A3o-Visual-Siconv/2.-Sum%C3%A1rio)
* [Showcase SICONV] (https://hom-siconv.estaleiro.serpro.gov.br/componentes/#/componentes)
* [Template README] (https://gist.githubusercontent.com/PurpleBooth/109311bb0361f32d87a2/raw/8254b53ab8dcb18afc64287aaddd9e5b6059f880/README-Template.md)


## Path FTP dos Scripts de Homologação (hom4)

```
/basic/BSA/BSA_AP_42811_HOM_SICONV4

```
## Path FTP dos Scripts de Produção (prod)

```
/basic/BSA/BSA_AP_42811_PRO_SICONV

```



## Merge no Git

### Para Trazer do Master para um branch

```
git clone < remote_repo > -- Baixa o Projeto.
git clone -b <branch> < remote_repo > - Baixa a branch do Projeto.
git checkout master -- Obtém os dados da origem do código fonte.
git pull -- Atualiza o repositório Local com os dados do master.
git checkout < branch desejado > -- Retorna ao branch para onde se deseja trazer o conteúdo.
git cherry-pick < hash git > -- Obtém a mudança que se deseja trazer, com base no identificador único.
git push -- Comita o conteúdo para o repositório remoto.

```

## DevOps

### Prático

Clique [aqui](https://git.serpro/dedat/deat4/pratico) para instalar o prático.

```
estaleiro logs d siconv-medicao-backend --system siconv -f -- Utiliza o cliente do estaleiro, melhor para ser usado quando utilizar -f (follow mode)
./pratico logs k8s -f --namespace siconv-d -- Lista os pods Ativos de um determinado ambiente para obtenção do log
./pratico logs k8s -f --namespace siconv-d --pod siconv-medicao-backend-7c9f977bd7-mwv2w -- Exibe o log de um pod específico. Id do pod utilizado como exemplo
./pratico view regrasAcesso --namespace siconv-d -- Cria um diagrama com as regras de acessos dos módulos de um ambiente.

```

## Issues

#### [Tamanho de Token](https://gitcorporativo.serpro/siconv-42811/Siconv-Medicao/siconv-medicao-frontend/-/issues/1)


## Medição gRPC 

#### [Readme](https://gitcorporativo.serpro/siconv-42811/Siconv-Medicao/siconv-medicao-grpc-server)


# SiconvStart

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 7.2.2.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
