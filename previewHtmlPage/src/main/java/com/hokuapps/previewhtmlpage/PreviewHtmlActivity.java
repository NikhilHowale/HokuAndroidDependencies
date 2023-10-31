package com.hokuapps.previewhtmlpage;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.print.PdfPrint;
import android.print.PrintAttributes;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.io.File;

public class PreviewHtmlActivity extends AppCompatActivity {

    WebView printWeb;
    public Toolbar toolbar;

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_htmltopdf);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this,R.color.purple_700)));
            getSupportActionBar().setTitle("");
        }


        final WebView webView = findViewById(R.id.webViewMain);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // initializing the printWeb Object
                printWeb = webView;
            }
        });
        webView.setVerticalScrollBarEnabled(true);


        String content = getIntent().getStringExtra("Content");

        if(content != null)
            webView.loadDataWithBaseURL(null, content, "text/html; charset=utf-8", "UTF-8", null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int itemId = item.getItemId();
        if (itemId == R.id.action_pdf) {
            if (printWeb != null) {
                String path = createWebPrintJob(printWeb);
                if (path != null) {
                    Toast.makeText(this, "Pdf saved", Toast.LENGTH_LONG).show();

                }
            } else {
                Toast.makeText(PreviewHtmlActivity.this, "WebPage not fully loaded", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (itemId == R.id.action_share) {
            String path = createWebPrintJob(printWeb);
            share(path);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method download pdf show in webView
     * @param webView webView
     * @return return downloaded pdf path
     */
    private String createWebPrintJob(WebView webView) {
        try {
            String Name = System.currentTimeMillis() + ".pdf";
            String jobName = getString(R.string.app_name) + " Document";
            PrintAttributes attributes = new PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build();
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/PDFTest/");
            PdfPrint pdfPrint = new PdfPrint(attributes);
            pdfPrint.print(webView.createPrintDocumentAdapter(jobName), path, Name);


            return path + "/" + Name;
        }catch (Exception e){
            e.printStackTrace();
        }
       return null;
    }

    /**
     * This method share pdf show in webView
     * @param dPath download pdf path
     */
    private void share(String dPath) {

        Uri path = Uri.parse(dPath);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, path);
        intent.setType("application/pdf");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}