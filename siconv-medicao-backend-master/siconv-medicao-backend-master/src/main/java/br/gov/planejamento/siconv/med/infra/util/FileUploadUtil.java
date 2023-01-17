package br.gov.planejamento.siconv.med.infra.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import lombok.Data;

@ApplicationScoped
public class FileUploadUtil {

    public List<FileUploadUtil.FileUpload> process(MultipartFormDataInput multipart) throws IOException
    {

        List<FileUploadUtil.FileUpload> listaArquivos = new ArrayList<>();

        for (Iterator<InputPart> iterator = multipart.getParts().iterator(); iterator.hasNext();)
        {
            InputPart part = iterator.next();

            processMultpart(multipart, listaArquivos, part);
        }

        return listaArquivos;
    }

	private void processMultpart(MultipartFormDataInput multipart, List<FileUploadUtil.FileUpload> listaArquivos,
			InputPart part) throws IOException {
		for (String chave : getContentDispositionRaw(part))
		{

		    String[] partes = chave.split(";");
		    for (int i = 0; i < partes.length; i++)
		    {
		    	boolean abort = false;

		        if (partes[i].length() > 8 && partes[i].substring(0, 9).contains("filename"))
		        {
		            FileUpload fu = new FileUploadUtil.FileUpload();

		            String[] arquivo = partes[i].split("=");

		            fu.setFileName(arquivo[1].replace("\"", ""));
		            fu.setContent(IOUtils.toByteArray(part.getBody(InputStream.class, null)));
		            listaArquivos.add(fu);
		            abort = true;
		        } else if (partes[i].contains("id="))
		        {
		            FileUpload fu = new FileUploadUtil.FileUpload();

		            String[] arquivo = partes[i].split("=");

		            fu.setId(Long.parseLong(arquivo[2].replace("\"", "")));
		            fu.setFileName(multipart.getFormDataPart("id=" + fu.getId(), String.class, null));
		            listaArquivos.add(fu);
		            abort = true;
		        }
		        
		        if (abort)
		        {
		        	break;
		        }
		    }
		}
	}

    private String[] getContentDispositionRaw(InputPart part) {

        String[] contentDisposition = null;

        try {
            Field field = part.getClass().getDeclaredField("bodyPart");
            field.setAccessible(true);
            Object bodyPart = field.get(part);

            Class<?>[] cArg = {};
            
            Method methodBodyPart = bodyPart.getClass().getMethod("getHeader", cArg);
            Iterable<?> iterable = (Iterable<?>) methodBodyPart.invoke(bodyPart, cArg);

            
            Object[] content = IteratorUtils.toArray(iterable.iterator());
            Method methodContent = content[0].getClass().getMethod("getRaw", cArg);

            contentDisposition = methodContent.invoke(content[0], cArg).toString().split(";");

        } catch (Exception e) {
            throw new IllegalArgumentException("Erro ao obter o ContentDisposition do InputPart.", e);
        }

        return contentDisposition;
    }

    @Data
    public class FileUpload {

        private byte[] content;
        private String fileName;
        private Long id;

    }

}
