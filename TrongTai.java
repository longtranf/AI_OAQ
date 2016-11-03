/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testSQuares2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author VPC
 */
class TrongTai {

    private final int DUNG = 0;
    private final int DI = 1;
    private final int AN = 2;
    private final int BOC = 3;
    private final int KIEM_TRA = 4;
    private final int DOI = 5;

    private final int NUMBER_IMAGE = 25;

    private Game game;
    public Player player;
    public Board board;
    private Image hand;
    int danInHand;
    private int x, y;         //Tọa độ bàn tay
    Step buocDi;
    int selected;
    int direction;
    int state;
    boolean eating = false;
    int count = 0; // bien dem de ve ban tay

    // Short Link
    House[] houseShortLink;
    Villa q0ShortLink;
    Villa q6ShortLink;
    Player p1ShortLink;
    Player p2ShortLink;

    public TrongTai(Game game) {
        this.game = game;
        x = game.board.START_X;
        y = game.board.START_Y;
        danInHand = 0;
        houseShortLink = game.board.houses;
        q0ShortLink = game.board.q0;
        q6ShortLink = game.board.q6;
        p1ShortLink = game.p1;
        p2ShortLink = game.p2;
        this.state = DOI;
        this.count = 0;
    }

    /**
     * Di Tung buoc
     */
    public void handle(Step buocDi) {

        // neu trang thai dang tu doi
        // duoc goi
        // den luot rai quan
        if (this.state == DOI) {
            this.selected = buocDi.chose;
            this.direction = buocDi.direc;
            this.danInHand = layQuan(this.selected);
            chuyenNhaKe(this.direction);
            System.out.println("So Dan : " + this.danInHand + " Selected : " + this.selected + " direction : " + this.direction);
            this.state = DI;
            setToaDo();
            return;
        }

        if (this.count < NUMBER_IMAGE) {
            tangToaDo();
            return;
        }
        this.count = 0;

        try {
            Thread.sleep(300);
        } catch (Exception e) {
        }
        // kiem tra trang thai cua trong tai
        switch (this.state) {

            case DUNG:
                this.state = DOI;
                this.eating = false;
                setTurnToken(buocDi);
                if (!checkContinueGame(board)) {
                    this.game.turnToken = 0;
                    return;
                }
                if (checkBoardPlayer(this.game.turnToken)) {
                    themDan(this.game.turnToken);
                }
                resetBuocDi();

                break;

            case DI:
                rai1Quan();
                chuyenNhaKe(direction);
                System.out.println("So Dan : " + this.danInHand + " Selected : " + this.selected + " direction : " + this.direction);
                setToaDo();
                break;

            case AN:
                An(buocDi, this.selected);
                chuyenNhaKe(direction);
                this.eating = true;
                this.state = KIEM_TRA;
                break;

            case BOC:

                break;

            case KIEM_TRA:
                int result = checkFinalHouse(this.selected, direction);
                System.out.println("Check : Selected : " + this.selected + " Direction : " + this.direction);
                System.out.println("Ket Qua Kiem Tra : " + result);
                switch (result) {

                    // Truong hop di tiep
                    case 1:
                        if (this.eating) {
                            this.state = DUNG;
                            break;
                        }
                        this.danInHand = layQuan(this.selected);
                        chuyenNhaKe(direction);
                        this.state = DI;
                        break;

                    // Truong hop An
                    case 2:
                        chuyenNhaKe(direction);
                        this.state = AN;
                        break;

                    // Truong hop dung
                    case 0:
                        this.state = DUNG;
                        break;

                    default:
                        break;
                }

                break;

        }

        return;
    }

    /**
     * Rai 1 Quan Neu So Quan Trong tay het Chuyen trang thai ban ve kiem tra
     */
    public void rai1Quan() {
        this.danInHand--;
        if (this.selected != 6 && this.selected != 0) {
            houseShortLink[this.selected].tangDanSo();
        } else if (this.selected == 0) {
            q0ShortLink.tangDanSo();
        } else {
            q6ShortLink.tangDanSo();
        }
        if (this.danInHand == 0) {
            this.state = KIEM_TRA;
        }
    }

    /**
     * Thuc hien buoc di va an quan
     *
     * @param buocDi (chose, direc)
     */
    public void oldhandle(Step buocDi) {

        int soDan;
        this.selected = buocDi.chose;

//        boolean flagAn = false;
        // Kiem tra so dan trong nha cuoi de di tiep
        while (true) {
            soDan = layQuan(this.selected);
            System.out.println("So Dan : " + soDan + " selected : " + selected);

            chuyenNhaKe(buocDi.direc);

            raiQuan(soDan, buocDi.direc);

            System.out.println("Result Check : " + checkFinalHouse(this.selected, buocDi.direc));
            if (checkFinalHouse(this.selected, buocDi.direc) != 1) {
                break;
            }
        }

        // TH An Dan
        while (checkFinalHouse(this.selected, buocDi.direc) == 2) {
            chuyenNhaKe(buocDi.direc);

            An(buocDi, this.selected);
            // Chuyen den o tiep theo de xet
            chuyenNhaKe(buocDi.direc);
            System.out.println("Selected : " + this.selected);
        }

        // set token de choi tiep
        setTurnToken(buocDi);
        System.out.println("Continue Check : " + checkContinueGame(board));
        if (!checkContinueGame(board)) {
            this.game.turnToken = 0;
        }

        // Trong Truong hop khong du quan de rai khi het dan trong cac o
        // Score < 5
        // thi game over
        if (this.game.turnToken != 0 && checkBoardPlayer(this.game.turnToken)) {
            themDan(this.game.turnToken);
        }

        resetBuocDi();
    }

    /**
     * Lay quan va set so quan la 0
     *
     * @param selected => O Dang xet
     * @return soQuan nhan duoc
     */
    public int layQuan(int selected) {
        int danSo = houseShortLink[selected].getDanSo();
        houseShortLink[selected].setDanSo(0);
//        repaint();
        return danSo;
    }

    public void chuyenNhaKe(int direction) {
        this.selected = tangNhaKe(direction, this.selected);
    }

    public int tangNhaKe(int direction, int current) {
        if (direction == 1) {
            if (current == 11) {
                current = 0;
            } else {
                current++;
            }
        } else if (current == 0) {
            current = 11;
        } else {
            current--;
        }

        return current;
    }

    /**
     * Lay So Dan va dai den khi het
     *
     * @param soDan => So Quan de rai
     * @param direction => Huong rai quan
     * @param selected => O Duoc chon
     * @return O Cuoi Cung khi het quan (so quan con lai de rai = 0)
     */
    public void raiQuan(int soDan, int direction) {

        for (int i = soDan; i > 0; i--) {

            // neu vao quan 0
            if (this.selected == 0) {
                q0ShortLink.tangDanSo();
            } // neu vao quan 6
            else if (this.selected == 6) {
                q6ShortLink.tangDanSo();
            } // neu khong vao quan
            else {
                houseShortLink[this.selected].tangDanSo();
            }
            chuyenNhaKe(direction);
        }

    }

    /**
     * Kiem tra o ket thuc
     *
     * @param selected
     * @param direction => Huong di de xet theo truong hop
     * @return 0 => Dung (2 O Trong Hoac Quan) 1 => Choi Tiep (O Co chua soi va
     * khong la quan) 2 => An Dan (O Trong sau do la 1 o dan co quan) 3 => An
     * Quan
     */
    public int checkFinalHouse(int current, int direction) {
        int checked;
        // vao o quan
        if (current == 0 || current == 6) {
            checked = tangNhaKe(direction, current);
            if (checkEmpty(checked)) {
                int checked_next = tangNhaKe(direction, checked);
                if (checkEmpty(checked_next)) {
                    return 0;
                }
                return 2;
            }
            return 0;
        }

        // Khong Gap O Quan
        // So Dan khac 0 Choi Tiep
        if (houseShortLink[current].getDanSo() != 0) {
            return 1;
        }

        // So dan o do = 0
        // So dan o tiep theo 
        checked = tangNhaKe(direction, current);
        // = 0 Dung
        // != 0 An
        if (checkEmpty(checked)) {
            return 0;
        } else {
            if (checked == 0 && q0ShortLink.coQuan && q0ShortLink.getDanSo() < 5) {
                return 0;
            }
            if (checked == 6 && q6ShortLink.coQuan && q6ShortLink.getDanSo() < 5) {
                return 0;
            }
            return 2;
        }
    }

    /**
     * Kiem tra nha trong hay khong
     *
     * @param current
     * @return
     */
    public boolean checkEmpty(int current) {
        if (current == 0) {
            if (q0ShortLink.coQuan || q0ShortLink.getDanSo() != 0) {
                return false;
            }
            return true;
        }
        if (current == 6) {
            if (q6ShortLink.coQuan || q6ShortLink.getDanSo() != 0) {
                return false;
            }
            return true;
        }
        if (houseShortLink[current].getDanSo() != 0) {
            return false;
        }
        return true;
    }

    public void An(Step buocDi, int current) {
        if (current == 0 || current == 6) {
            AnQuan(buocDi, current);
        } else {
            AnDan(buocDi, current);
        }
    }

    /**
     * An Dan
     *
     * @param selected
     * @param direction => Chieu an quan
     * @return
     */
    public void AnDan(Step buocDi, int current) {

        if (isPlayer1(buocDi)) {
            p1ShortLink.currentScore += layQuan(current);

        } else {
            p2ShortLink.currentScore += layQuan(current);
        }

    }

    public void AnQuan(Step buocDi, int current) {

        // Quan 0
        if (current == 0) {
            if (isPlayer1(buocDi)) {
                p1ShortLink.currentScore = p1ShortLink.currentScore + q0ShortLink.getDanSo();
                q0ShortLink.setDanSo(0);
                if (q0ShortLink.coQuan) {
                    q0ShortLink.coQuan = false;
                    p1ShortLink.currentScore += 10;
                }
            } else {
                p2ShortLink.currentScore = p2ShortLink.currentScore + q0ShortLink.getDanSo();
                q0ShortLink.setDanSo(0);
                if (q0ShortLink.coQuan) {
                    q0ShortLink.coQuan = false;
                    p2ShortLink.currentScore += 10;
                }
            }
        } // Quan 6
        else if (isPlayer1(buocDi)) {
            p1ShortLink.currentScore = p1ShortLink.currentScore + q6ShortLink.getDanSo();
            q6ShortLink.setDanSo(0);
            if (q6ShortLink.coQuan) {
                q6ShortLink.coQuan = false;
                p1ShortLink.currentScore += 10;
            }
        } else {
            p2ShortLink.currentScore = p2ShortLink.currentScore + q6ShortLink.getDanSo();
            q6ShortLink.setDanSo(0);
            if (q6ShortLink.coQuan) {
                q6ShortLink.coQuan = false;
                p2ShortLink.currentScore += 10;
            }
        }

    }

    public boolean checkContinueGame(Board board) {
        if (checkEmpty(0) && checkEmpty(6)) {
            return false;
        }
        return true;
    }

    public boolean isPlayer1(Step buocDi) {
        if (buocDi.chose > 6) {
            return true;
        }
        return false;
    }

    /**
     * Turn tiep theo la player 1
     *
     * @param token
     * @return true neu dung false neu sai
     */
    public boolean nextTurnIsPlayer1(int token) {
        if (token == 1) {
            return true;
        }
        return false;
    }

    // set token cho luot tiep theo
    public void setTurnToken(Step buocDi) {

        if (isPlayer1(buocDi)) {
            game.turnToken = 2;
        } else {
            game.turnToken = 1;
        }
    }

    // kiem tra 
    // neu so quan tren ban het thi them
    public boolean checkBoardPlayer(int token) {
        boolean allEmpty = true;
        if (nextTurnIsPlayer1(token)) {
            for (int i = 7; i <= 11; i++) {
                if (houseShortLink[i].getDanSo() != 0) {
                    allEmpty = false;
                }
            }
        } else {
            for (int i = 1; i < 6; i++) {
                if (houseShortLink[i].getDanSo() != 0) {
                    allEmpty = false;
                }
            }
        }
        return allEmpty;
    }

    /**
     * Them 1 dan vao moi o cua player co luot tiep
     *
     * @param token
     */
    public void themDan(int token) {
        if (nextTurnIsPlayer1(token)) {
            for (int i = 7; i <= 11; i++) {
                houseShortLink[i].setDanSo(1);
            }
            // Tru diem cua player
            if (p1ShortLink.currentScore < 5) {
                this.game.turnToken = 0;
            }
            p1ShortLink.currentScore -= 5;
        } else {
            for (int i = 1; i <= 5; i++) {
                houseShortLink[i].setDanSo(1);
            }
            // Tru diem cua player
            if (p2ShortLink.currentScore < 5) {
                this.game.turnToken = 0;
            }
            p2ShortLink.currentScore -= 5;
        }
    }

    public void resetBuocDi() {
        p1ShortLink.resetBuocDi();
        p2ShortLink.resetBuocDi();
    }

    public void setToaDo() {
        if (this.direction == 1) {
            if (this.selected < 6 && this.selected != 0) {
                this.x = game.board.START_X + (this.selected - 2) * 100;
                this.y = game.board.START_Y;

            } else if (this.selected > 6) {
                this.x = game.board.START_X + (11 - this.selected + 1) * 100;
                this.y = game.board.START_Y + 102;
            } else if (this.selected == 0) {
                this.x = game.board.START_X - 100;
                this.y = game.board.START_Y + 50;
            } else {
                this.x = game.board.START_X + 503;
                this.y = game.board.START_Y + 50;
            }
        }
        else {
            if (this.selected < 6 && this.selected != 0) {
                this.x = game.board.START_X + this.selected * 100;
                this.y = game.board.START_Y;

            } else if (this.selected > 6) {
                this.x = game.board.START_X + (11 - this.selected - 1) * 100;
                this.y = game.board.START_Y + 102;
            } else if (this.selected == 0) {
                this.x = game.board.START_X - 100;
                this.y = game.board.START_Y + 50;
            } else {
                this.x = game.board.START_X + 503;
                this.y = game.board.START_Y + 50;
            }
        }
    }

    public void tangToaDo() {
        if (this.direction == -1) {
            this.count++;
            if (this.selected > 6) {
                this.x += 4;
            } else if (this.selected < 6 && this.selected != 0) {
                this.x -= 4;
            } else {
                setToaDo();
            }
        } else {
            this.count++;
            if (this.selected > 6) {
                this.x -= 4;
            } else if (this.selected < 6 && this.selected != 0) {
                this.x += 4;
            } else {
                setToaDo();
            }
        }
    }

    public void paint(Graphics2D g2d) {

        if (this.state == DI) {

            g2d.drawImage(Game.namTay, this.x, this.y - 50, null);
            g2d.setColor(Color.yellow);
            g2d.drawString(String.valueOf(danInHand), this.x+43 , this.y-50+38);

        }

    }

}
