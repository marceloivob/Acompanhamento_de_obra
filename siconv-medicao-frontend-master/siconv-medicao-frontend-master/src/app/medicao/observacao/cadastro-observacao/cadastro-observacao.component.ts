import { Profile } from '../../../shared/model/security/profile.enum';
import { Permission } from '../../../shared/model/security/permission.enum';
import { RequiredAuthorizer } from '../../../shared/model/security/required-authorizer.model';
import { BaseComponent } from 'src/app/shared/util/base.component';
import { Component, Injector } from '@angular/core';
import { FormGroup, FormBuilder, FormControl, Validators } from '@angular/forms';
import { Observacao } from 'src/app/shared/model/observacao.model';
import { ObservacaoService } from 'src/app/shared/services/observacao.service';
import { Anexo } from 'src/app/shared/model/anexo.modelo';
import { Router, ActivatedRoute } from '@angular/router';
import { Medicao } from 'src/app/shared/model/medicao.model';
import { CnpjPipe } from 'src/app/shared/pipes/cnpj.pipe';
import { EmpresaService } from 'src/app/shared/services/empresa.service';
import { SharedService } from 'src/app/shared/services/shared-service.service';
import { tipoArquivoValidator, tamanhoArquivoValidator, tamanhoNomeArquivoValidator } from 'src/app/shared/validators/util-validator';
import { FileUtil } from 'src/app/shared/util/file-util';
import { Role } from 'src/app/shared/model/security/role.enum';

@Component({
  selector: 'app-cadastro-observacao',
  templateUrl: './cadastro-observacao.component.html',
  styleUrls: ['./cadastro-observacao.component.scss']
})
export class CadastroObservacaoComponent extends BaseComponent {

      cadastrarObservacao =  new FormGroup({});
      tituloAnexo = 'Anexos';
      tituloObservacao = 'Observação';
      subtitulo = '';
      operacao = '';

      ///// Variaveis do processo de salvamento
      observacaoGravada: Observacao;
      /////////////////////////////////////

      ///// Variaveis do objeto Siconv-Table
      fileExportName = 'listaArqAnexados';
      data = [];
      exportFile = [{'column': 'txArquivo', 'key': 'txArquivo'}];
      lista: any[];
      idPosNomeArquivo = 1;
      //////////////////////////////////////

      ////// Variáveis do file
      nomeArquivo = '';
      anexoAtual: Anexo;

      idMedicao: number;

      medicao: Medicao;
      observacao: Observacao;

      fileCtrl = new FormControl('');
      descricaoCtrl: FormControl;

      file: any;

      constructor(
        private formBuilder: FormBuilder,
        private _observacaoService: ObservacaoService,
        private _router: Router,
        private _cnpj: CnpjPipe,
        private _empresaService: EmpresaService,
        private _sharedService: SharedService,
        private route: ActivatedRoute,
        private injector: Injector
      ) {
        super (injector);
      }

      loadPermissions(): Map <string, RequiredAuthorizer> {
        const profiles = [Profile.EMPRESA, Profile.PROPONENTE, Profile.CONCEDENTE, Profile.MANDATARIA];
        const roles = [Role.GESTOR_FINANCEIRO_CONVENENTE, Role.OPERADOR_FINANCEIRO_CONVENENTE, Role.GESTOR_CONVENIO_CONVENENTE, Role.FISCAL_CONVENENTE,
          Role.FISCAL_CONCEDENTE, Role.GESTOR_CONVENIO_CONCEDENTE, Role.GESTOR_FINANCEIRO_CONCEDENTE, Role.OPERACIONAL_CONCEDENTE,
          Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA, Role.FISCAL_ACOMPANHAMENTO, Role.TECNICO_TERCEIRO];

        return new Map([
          ['editar', new RequiredAuthorizer (profiles, roles, [Permission.EDITAR_OBSERVACAO_MEDICAO])],
          ['incluir', new RequiredAuthorizer (profiles, roles, [Permission.INCLUIR_OBSERVACAO_MEDICAO])]
        ]);
      }

      initializeComponent() {

        const paramMap = this.route.snapshot.paramMap;

        this.idMedicao = +paramMap.get('idMedicao');

        this.observacao = this.route.snapshot.data.obs;

        if (!this.observacao) {
          this.operacao = 'I';
        }

        this.medicao = this.route.snapshot.data.medicao;
        this.emitirTitulo(this.medicao.sequencial);

        this.carregaForm();
        this.inicializarObservacao();
      }

      carregaForm() {

        this.descricaoCtrl = new FormControl(null, Validators.required);
        this.fileCtrl = new FormControl(null, [tipoArquivoValidator, tamanhoArquivoValidator, tamanhoNomeArquivoValidator]);

        this.cadastrarObservacao = this.formBuilder.group({txObservacao: this.descricaoCtrl,
                                                          file: this.fileCtrl});
      }

      inicializarObservacao() {
        if (this.observacao) {

            this.cadastrarObservacao.controls['txObservacao'].setValue(this.observacao.txObservacao);

            this.carregarListaAnexos();
        }

      }

      // Carrego a lista de anexos no objeto data que é vinculado ao siconv-table
      carregarListaAnexos() {

        if (this.observacao.anexos) {
          this.observacao.anexos.forEach(element => {
            this.data.push(element);
            this.idPosNomeArquivo += 1;
          });
        }
      }

      fileUpload(event) {

        if (event.target.files.length > 0) {
          if (FileUtil.validarArquivo(event.target.files[0], 10, this.cadastrarObservacao.get('file'))) {
            const anexo = new Anexo();
            anexo.arquivo = event.target.files[0];
            anexo.nmArquivo = anexo.arquivo['name'];
            this.anexoAtual = anexo;
          } else {
            this.anexoAtual = null;
          }
        } else {
          this.anexoAtual = null;
        }
      }


      anexarArquivo() {

        if (this.anexoAtual) {
          this.data = [...this.data, this.anexoAtual];

          // Limpar nome do arquivo no formprivate _empresaService: EmpresaService,
          this.limparCampoUpload();
        }

      }

      private limparCampoUpload() {
        this.fileCtrl.setValue(null);
        this.anexoAtual = null;
      }

      cancelar() {
        this._router.navigate(['./observacao/listar'], {relativeTo: this.route.parent});
      }

      salvarObservacao() {

        let observacao;

        const control = this.cadastrarObservacao.get('txObservacao');

        if (!control.valid) {
          control.markAsTouched({ onlySelf: true });
        } else {
            if (!this.observacao) {
                  observacao = new  Observacao(null,
                                              null,
                                              null,
                                              null,
                                              null,
                                              this.cadastrarObservacao.value.txObservacao,
                                              this.idMedicao,
                                              this.data);

                  this._observacaoService.incluirObservacao(observacao, this.idMedicao.toString()).subscribe(value => {
                              this.observacaoGravada = value;
                              super.adicionarMensagem ('Observação salva com sucesso.', true);
                              this._router.navigate(['./observacao/listar'], {relativeTo: this.route.parent});
                  }, error => {});

            } else {
                  this.observacao.txObservacao = this.cadastrarObservacao.value.txObservacao;
                  this.observacao.anexos = this.data;

                  this._observacaoService.alterarObservacao(this.observacao,
                                                            this.observacao.medicaoFk.toString()).subscribe(value => {
                              this.observacaoGravada = value;
                              super.adicionarMensagem ('Observação salva com sucesso.', true);
                              this._router.navigate(['./observacao/listar'], {relativeTo: this.route.parent});
                  }, error => {});
            }

        }
      }

      excluirAnexo(item: Anexo) {
          const removeAnexo: Anexo = item;
          this.data.splice(this.data.indexOf(removeAnexo), 1);

          // Força uma atualização da ref. da variável para o Angular detectar a mudança
          this.data = this.data.slice();
      }

      getListaPaginada(listap) {
          this.lista = listap;
      }

      emitirTitulo(idMedicao: number) {
        if (this.operacao === 'I') {
          this.subtitulo = 'Incluir Observação';
        } else {
          this.subtitulo = 'Alterar Observação';
        }

        const tituloTela = {
          titulo: 'Medição ' + idMedicao,
          subtitulo: this.subtitulo,
          info: this._cnpj.transform(this._empresaService.empresaAtual.cnpj) + ' - ' + this._empresaService.empresaAtual.razaoSocial
        };

        this._sharedService.emitChange(tituloTela);
      }
}
