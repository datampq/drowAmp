/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drowamp;

import java.io.File;
import java.util.LinkedList;

/**
 * http://drow.today
 *
 * @author datampq
 */
public class manager {

    public final main main;
    private int index;
    private LinkedList<File> files;
    private boolean shuffle = false;
    private boolean repeat = false;
    private boolean play = false;

    public manager(main m) {
        files = new LinkedList();
        index = 0;
        main = m;
    }

    public void addFile(File f) {
        files.add(f);
        main.addItemToPlaylist(f.getName());
    }

    public void PlayFile(String name) {
        for (File f : files) {
            if (f.getName().equals(name)) {
                main.encoder.Play(f.getPath());
                index = files.indexOf(f);
                break;
            }
        }
    }

    public void setShuffle() {
        shuffle = !shuffle;
    }

    public void setRepeat() {
        repeat = !repeat;
    }

    public void getNext() {
        main.deselectAll();
        if (shuffle) {
            index = (int) (Math.random() * files.size());
            main.select(files.get(index).getName());
            main.encoder.Play(files.get(index).getPath());
        } else {
            if (index < files.size() - 1) {
                index++;
            } else {
                index = 0;
            }
            main.select(files.get(index).getName());
            main.encoder.Play(files.get(index).getPath());
        }
    }

    public void getPrev() {
        main.deselectAll();
        if (shuffle) {
            index = (int) (Math.random() * files.size());
            main.select(files.get(index).getName());
            main.encoder.Play(files.get(index).getPath());
        } else {
            if (index > 0) {
                index--;
            } else {
                index = files.size() - 1;
            }
            main.select(files.get(index).getName());
            main.encoder.Play(files.get(index).getPath());
        }
    }

    public void getPlay() {
        main.deselectAll();
        if (main.encoder.inited) {
            if (play) {
                main.encoder.current.pause();
                play = false;
            } else {
                main.encoder.current.play();
                play = true;
            }
        } else {
            main.encoder.Play(getFile());
            play = true;
        }
    }

    public void getStop() {
        if (!stopped) {
            main.encoder.stop();
            stopped = true;
        } else {
            main.encoder.resume();
            stopped = false;
        }
    }
    private boolean stopped = false;

    public String getFile() {
        main.deselectAll();
        if (repeat) {
            return files.get(index).getPath();
        } else {
            if (shuffle) {
                index = (int) (Math.random() * files.size());
                File f = files.get(index);
                main.select(f.getName());
                return f.getPath();
            } else {
                if (index > files.size() - 1) {
                    index = 0;
                } else {
                    index++;
                }
                File f = files.get(index);
                main.select(f.getName());
                return f.getPath();
            }
        }
    }
}
