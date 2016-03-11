package org.freesnap.main.shell.capturetool;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.*;
import org.freesnap.FreeSnap;
import org.freesnap.util.image.Helper;
import org.freesnap.util.video.Encoder;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

//TODO: refactor, optimize, should not store ArrayList<Image>
public class VideoCaptureTool extends AbstractCaptureTool {
    protected Shell shell;
    private Rectangle rect;
    private boolean recording;
    private boolean upload = false;
    private Button btn;
    private ArrayList<org.eclipse.swt.graphics.Image> frames;
    private int frameTimeInMs;
    private int i = 0;

    public VideoCaptureTool() {
        init();
    }

    private void init() {
        this.shell = new Shell(Display.getCurrent(), SWT.NO_TRIM | SWT.ON_TOP);
        this.shell.setLocation(0, 0);
        this.shell.forceActive();
        this.shell.forceFocus();
        this.shell.setBounds(Display.getCurrent().getBounds());
        this.frames = new ArrayList<Image>();
        this.frameTimeInMs = (int) ((float) 1000 / (float) 25);
    }

    @Override
    public void open(Rectangle rect) {
        this.rect = rect;
        shell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
        shell.setLocation(0, 0);
        Region region = new Region();
        region.add(rect.x - 2, rect.y - 2, rect.width + 4, rect.height + 4);
        region.add(rect.x - 2, rect.y - 34, 206, 34);
        region.subtract(rect);
        shell.setRegion(region);

        recording = false;
        btn = new Button(shell, SWT.PUSH);
        btn.setText("Start");
        btn.pack();
        btn.setLocation(rect.x, rect.y - 32);
        btn.setSize(56, 30);
        btn.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                if (!recording) {
                    recording = true;
                    try {
                        startRecording();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        Button saveBtn = new Button(shell, SWT.PUSH);
        saveBtn.setText("Save");
        saveBtn.pack();
        saveBtn.setLocation(rect.x + 58, rect.y - 32);
        saveBtn.setSize(56, 30);
        saveBtn.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                upload = false;
                recording = false;
            }
        });

        Button uploadBtn = new Button(shell, SWT.PUSH);
        uploadBtn.setText("Upload");
        uploadBtn.pack();
        uploadBtn.setLocation(rect.x + 116, rect.y - 32);
        uploadBtn.setSize(56, 30);
        uploadBtn.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                upload = true;
                recording = false;
            }
        });

        Button close = new Button(shell, SWT.PUSH);
        close.setText("X");
        close.pack();
        close.setLocation(rect.x + 174, rect.y - 32);
        close.setSize(28, 30);
        close.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                if (recording) {
                    recording = false;
                }
                shell.close();
            }
        });


        this.shell.open();
    }

    private void startRecording() throws IOException {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        final String fileName = dateFormat.format(Calendar.getInstance().getTime()) + ".mp4";
        final File out = new File(fileName);
        final Encoder encoder = new Encoder(out, rect.width, rect.height);
        final long startTimeInMs = System.currentTimeMillis();

        Runnable timer = new Runnable() {
            public void run() {
                long frameStartTime = System.currentTimeMillis();
                Display current = Display.getCurrent();
                GC gc = new GC(current);
                org.eclipse.swt.graphics.Image image = new org.eclipse.swt.graphics.Image(current, rect.width, rect.height);
                gc.copyArea(image, rect.x, rect.y);
                gc.dispose();
                gc = new GC(image);

                gc.setBackground(current.getSystemColor(SWT.COLOR_WHITE));
                gc.fillOval(current.getCursorLocation().x - rect.x - 1, current.getCursorLocation().y - rect.y - 1, 10, 10);
                gc.setBackground(current.getSystemColor(SWT.COLOR_BLACK));
                gc.fillOval(current.getCursorLocation().x - rect.x, current.getCursorLocation().y - rect.y, 8, 8);
                gc.dispose();

                frames.add(image);
                if (recording) {
                    btn.setText((((float) (System.currentTimeMillis() - startTimeInMs)) / (float) 1000) + "s");
                    int delay = (int) (frameTimeInMs - ((System.currentTimeMillis() - frameStartTime)));
                    current.timerExec(Math.max(0, delay), this);
                    System.out.println(System.currentTimeMillis() + " " + ++i);
                } else {
                    for (Image frame : frames) {
                        try {
                            encoder.encodeImage(Helper.convertToAWT(frame.getImageData()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        frame.dispose();
                    }
                    try {
                        encoder.finish();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    FreeSnap.processVideo(out.getName(), upload);
                }
            }
        };
        Display.getCurrent().timerExec(frameTimeInMs, timer);
    }
}
