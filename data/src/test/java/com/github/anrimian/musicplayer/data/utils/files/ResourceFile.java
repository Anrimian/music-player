package com.github.anrimian.musicplayer.data.utils.files;

import org.junit.rules.ExternalResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class ResourceFile extends ExternalResource
{
    String res;
    File file = null;
    InputStream stream;

    public ResourceFile(String res)
    {
        this.res = res;
    }

    public File getFile() throws IOException
    {
        if (file == null)
        {
            createFile();
        }
        return file;
    }

    public InputStream getInputStream()
    {
        return stream;
    }

    public InputStream createInputStream()
    {
        return getClass().getResourceAsStream(res);
    }

    public String getContent() throws IOException
    {
        return getContent("utf-8");
    }

    public String getContent(String charSet) throws IOException
    {
        InputStreamReader reader = new InputStreamReader(createInputStream(),
                Charset.forName(charSet));
        char[] tmp = new char[4096];
        StringBuilder b = new StringBuilder();
        try
        {
            while (true)
            {
                int len = reader.read(tmp);
                if (len < 0)
                {
                    break;
                }
                b.append(tmp, 0, len);
            }
            reader.close();
        }
        finally
        {
            reader.close();
        }
        return b.toString();
    }

    @Override
    protected void before() throws Throwable
    {
        super.before();
        stream = getClass().getResourceAsStream(res);
    }

    @Override
    protected void after()
    {
        try
        {
            stream.close();
        }
        catch (IOException e)
        {
            // ignore
        }
        if (file != null)
        {
            file.delete();
        }
        super.after();
    }

    private void createFile() throws IOException
    {
        file = new File(".",res);
        InputStream stream = getClass().getResourceAsStream(res);
        try
        {
            file.createNewFile();
            FileOutputStream ostream = null;
            try
            {
                ostream = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                while (true)
                {
                    int len = stream.read(buffer);
                    if (len < 0)
                    {
                        break;
                    }
                    ostream.write(buffer, 0, len);
                }
            }
            finally
            {
                if (ostream != null)
                {
                    ostream.close();
                }
            }
        }
        finally
        {
            stream.close();
        }
    }

}