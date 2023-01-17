package br.gov.planejamento.siconv.med.test.extension;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.gov.planejamento.siconv.med.infra.database.DAOFactory;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;

public abstract class BusinessControllerBaseTest extends SecurityBaseTest {

    @Mock
    protected Jdbi jdbi;

    @Mock
    protected DAOFactory dao;

    @Mock
    protected Handle handle;

    @BeforeEach
    void setupJdbiMock() throws Exception {

        MockitoAnnotations.openMocks(this).close();

        when(dao.getJdbi()).thenReturn(jdbi);

        when(jdbi.inTransaction(Mockito.any())).then(invocation -> {
            HandleCallback<?, ?> callback = invocation.getArgument(0);
            return callback.withHandle(handle);
        });

        doAnswer(invocation -> {
            HandleConsumer<?> consumer = invocation.getArgument(0);
            consumer.useHandle(handle);
            return null;
        }).when(jdbi).useTransaction(Mockito.any());

    }

    protected final <T> void setupDaoMock(Class<T> daoClass, T daoMockInstance) {
        when(handle.attach(daoClass)).thenReturn(daoMockInstance);
        when(jdbi.onDemand(daoClass)).thenReturn(daoMockInstance);
        when(dao.get(daoClass)).thenReturn(daoMockInstance);
    }

    protected void assertThrowsMedicaoRestException(MessageKey expectedMessageKey, Executable executable) {
        assertThrowsMedicaoRestException(expectedMessageKey, null, executable);
    }

    protected void assertThrowsMedicaoRestException(MessageKey expectedMessageKey, List<String> expectedArguments,
            Executable executable) {

        MedicaoRestException exception = assertThrows(MedicaoRestException.class, executable);

        exception.getMessages().stream().findFirst().ifPresentOrElse(actualMessage -> {
            assertEquals(expectedMessageKey, actualMessage.getKey());
            assertEquals(expectedArguments, asList(actualMessage.getArguments()));
        }, () -> fail(format("A messageKey esperada era %s, mas nenhuma foi obtida", expectedMessageKey)));
    }

    private List<String> asList(String[] array) {
        return array != null ? Arrays.asList(array) : null;
    }
}
