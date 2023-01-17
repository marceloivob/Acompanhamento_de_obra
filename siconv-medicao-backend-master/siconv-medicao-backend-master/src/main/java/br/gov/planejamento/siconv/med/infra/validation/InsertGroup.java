package br.gov.planejamento.siconv.med.infra.validation;

import javax.validation.groups.Default;

/**
 * Esta interface representa um grupo de validação da especificação Bean
 * Validation do Java (JSR 303). Ver:
 * <ul>
 * <li>http://docs.jboss.org/hibernate/validator/4.2/reference/en-US/html_single/</li>
 * <li>http://beanvalidation.org/1.0/spec/</li>
 * <li>https://jcp.org/en/jsr/detail?id=303</li>
 * <li>http://google.com - Bean Validation</li>
 * <li>https://expressodrive.serpro.gov.br/index.php/s/v7jpZEJoDKzA3A4 -
 * Capítulo 3 do Livro <strong>Beginning Java EE 7</strong></li>
 * </ul>
 *
 * Representa o grupo de validação do fluxo de inclusão.
 *
 * @author SERPRO
 *
 */
public interface InsertGroup extends Default {

}