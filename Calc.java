/*
 * The MIT License
 *
 * Copyright 2020 Amr Medhat Alfayoumy.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package calc;

/**
 *
 * @author Amr
 */
import java.util.Scanner;

class data {

    double Load;
    double LoadPhase;
    double Line;
    double LinePhase;

    data(double mLoad, double pLoad, double mLine, double pLine) {
        this.Load = mLoad;
        this.LoadPhase = pLoad;
        this.Line = mLine;
        this.LinePhase = pLine;
    }
}

public class Calc {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);
        double v_an, v_ab, v_an_ph, v_ab_ph, z_y, z_d, z_ph, I_a, I_AB, I_a_ph, I_AB_ph, zTL, zTL_ph;
        v_an = v_ab = v_an_ph = v_ab_ph = z_y = z_d = z_ph = zTL = zTL_ph = I_a = I_AB = I_a_ph = I_AB_ph = 0;
        double zReal = 0, zImg = 0, imReal = 0, imImg = 0;
                
        data Voltage = new data(v_an, v_an_ph, v_ab, v_ab_ph);
        data Current = new data(I_AB, I_AB_ph, I_a, I_a_ph);

        System.out.println("Choose phase configuration: \n"
                + "[1] Single-Phase\n"
                + "[2] Three-Phase");
        int phase_choice = input.nextInt();
        System.out.print("\n\n");
        if(phase_choice == 1) {
            calculateSinglePhase(input, Voltage, zReal, zImg, imReal, imImg, z_ph, Current);
            return;        }
        
        System.out.println("Choose source configuration: \n"
                + "[1] Wye source\n"
                + "[2] delta source");
        int src_choice = input.nextInt();
        System.out.print("\n\n");

        System.out.println("Choose type of voltage input: \n"
                + "[1] Phase voltage\n"
                + "[2] Line voltage");
        int voltage_type = input.nextInt();
        System.out.print("\n\n");

        System.out.println("Choose load configuration: \n"
                + "[1] Wye load\n"
                + "[2] delta load");
        int load_choice = input.nextInt();
        
        System.out.print("\n\n");
        System.out.println("Choose type of impedances input form: \n"
                + "[1] Rectangular (z = x + yj)\n"
                + "[2] Polar (z = r @ theta)");
        int load_type = input.nextInt();
        
        inputVoltage(src_choice, voltage_type, Voltage, input);
        System.out.print("\n\n");
        
        double[] impedences = inputImpedences(load_type, zReal, zImg, imReal, 
                imImg, z_ph, z_d, z_y, zTL, zTL_ph, load_choice, input);
        System.out.print("\n\n");
        calculateCurrent(src_choice, load_choice, Voltage, Current, impedences);
    }
    
    static void calculateSinglePhase(Scanner input, data Voltage,
            double zReal, double zImg, double imReal, double imImg, double z_ph, data Current) {
                System.out.println("Choose type of impedances input form: \n"
                + "[1] Rectangular\n"
                + "[2] Phasor");
        int load_type = input.nextInt();
        System.out.println("\nEnter Voltage magnitude & phase: ");
        Voltage.Load = input.nextDouble();
        Voltage.LoadPhase = input.nextDouble();
        
        //Input impedances
        double[] impedences = inputImpedences(load_type, zReal, zImg, imReal, imImg, z_ph, 0, 0, 0, 0, 1, input);
        double zTotal = impedences[0];
        double zTotal_ph = impedences[1];
        double zTL = impedences[7];
        double zTL_ph = impedences[8];
        z_ph = impedences[2];  
        
        //Calculate current
        Voltage.Line = Voltage.Load;
        Current.Line = Voltage.Load / zTotal;
        Current.LinePhase = Voltage.LoadPhase - zTotal_ph;
        Current.Load = Current.Line;
        Current.LoadPhase = Current.LinePhase;
        
        //Calculate power
        zReal = impedences[3];
        zImg = impedences[4];
        imReal = impedences[5];
        imImg = impedences[6];
        double Pf = Math.cos(Math.toRadians(z_ph));
        double P = Current.Load * Current.Load * zReal;
        double Q = Current.Load * Current.Load * zImg;
        double P_losses = Current.Line * Current.Line * imReal;
        double Q_losses = Current.Line * Current.Line * imImg;
        double S_losses = Math.sqrt(Math.pow(P_losses, 2)+Math.pow(Q_losses, 2));
        double S = Math.sqrt(Math.pow(Q, 2)+Math.pow(P, 2));
        double res[] = {Pf, P, Q, S, P_losses, Q_losses, S_losses};
        
        //Print current
        System.out.print("\n" + "I = " + Current.Line + " A     " + "Phase : " + Current.LinePhase + "°\n");
        System.out.println();
        
        //Calculate and print voltage drops
        double z = Math.sqrt((zReal*zReal)+(zImg*zImg));
        double phi = (Current.LoadPhase+z_ph);
        double V = Current.Load*z;
        System.out.print("\nVoltage drop across load\n"
                + "V_load = " + V + " V     " + "Phase : " + (phi) + "°\n");
        System.out.println();
        double psi = (Current.LinePhase+zTL_ph);
        double V_TL = Current.Line * zTL;
        System.out.print("\nVoltage drop across line impedance\n"
                + "V_TL = " + V_TL + " V     " + "Phase : " + (psi) + "°\n");

        //Print power
        printPower(res, z_ph);
    }
    
    static void inputVoltage(int src_choice, int voltage_type, data Voltage, Scanner input) {
        System.out.println("\nEnter Voltage magnitude & phase: ");
        
        //Wye source
        if (src_choice == 1 && voltage_type == 1) {
            Voltage.Load = input.nextDouble();
            Voltage.LoadPhase = input.nextDouble();
            Voltage.Line = Voltage.Load * Math.sqrt(3);
            Voltage.LinePhase = Voltage.LoadPhase + 30;
        }
        if (src_choice == 1 && voltage_type == 2) {
            Voltage.Line = input.nextDouble();
            Voltage.LinePhase = input.nextDouble();
            Voltage.Load = Voltage.Line / Math.sqrt(3);
            Voltage.LoadPhase = Voltage.LinePhase - 30;
        }
        
        //Delta source
        if (src_choice == 2) {
            if (voltage_type == 1) {
                Voltage.Load = input.nextDouble();
                Voltage.LoadPhase = input.nextDouble();
                    Voltage.Line = Voltage.Load;
                    Voltage.LinePhase = Voltage.LoadPhase;
            }
            if (voltage_type == 2) {
                Voltage.Line = input.nextDouble();
                Voltage.LinePhase = input.nextDouble();
                Voltage.Load = Voltage.Line;
                Voltage.LoadPhase = Voltage.LinePhase;
            }
        }
    }
    
    static double[] inputImpedences(int load_type, double zReal, double zImg, double imReal, double imImg,
            double z_ph, double z_d, double z_y, double zTL, double zTL_ph, int load_choice, Scanner input) {
        double zTotal = 0, zTotal_ph = 0;
        
        //Rectangular form
        if (load_type == 1) {
            System.out.println("\nEnter Load real & imaginary components: ");
            zReal = input.nextDouble();
            zImg = input.nextDouble();
            z_ph = Math.toDegrees(Math.atan(zImg / zReal));
            System.out.println("\nEnter TL impedence real & imaginary components: ");
            imReal = input.nextDouble();
            imImg = input.nextDouble();
            zTL = Math.sqrt((Math.pow(imReal, 2) + Math.pow(imImg, 2)));
            zTL_ph = Math.toDegrees(Math.atan(imImg / imReal));
            
                //Calculate total impedance
                if (load_choice == 1) { //Wye load
                    zTotal = Math.sqrt((Math.pow(zReal + imReal, 2) + Math.pow(zImg + imImg, 2)));
                    zTotal_ph = Math.toDegrees(Math.atan( (zImg + imImg) / (zReal + imReal) ));
                }
                if (load_choice == 2) { //Delta load
                    zTotal = Math.sqrt((Math.pow(zReal/3 + imReal, 2) + Math.pow(zImg/3 + imImg, 2)));
                    zTotal_ph = Math.toDegrees(Math.atan( (zImg/3 + imImg) / (zReal/3 + imReal) ));
                }
        }
        
        //Polar form
        if (load_type == 2) {
            switch (load_choice) {
                case 1: //Wye load calculations
                    System.out.println("\nEnter Load magnitude & phase: ");
                    z_y = input.nextDouble();
                    z_ph = input.nextDouble();
                    System.out.println("\nEnter TL impedence magnitude & phase: ");
                    zTL = input.nextDouble();
                    zTL_ph = input.nextDouble();
                    zReal = z_y*Math.cos(Math.toRadians(z_ph));
                    zImg = z_y*Math.sin(Math.toRadians(z_ph));
                    break;

                case 2: //Delta load calculations
                    System.out.println("\nEnter Load magnitude & phase: ");
                    z_d = input.nextDouble();
                    z_ph = input.nextDouble();
                    System.out.println("\nEnter TL impedence magnitude & phase: ");
                    zTL = input.nextDouble();
                    zTL_ph = input.nextDouble();
                    zReal = (z_d/3)*Math.cos(Math.toRadians(z_ph));
                    zImg = (z_d/3)*Math.sin(Math.toRadians(z_ph));
                    break;

                default:
                    break;
            }
                    //Calculate total impedance
                    imReal = zTL*Math.cos(Math.toRadians(zTL_ph));
                    imImg = zTL*Math.sin(Math.toRadians(zTL_ph));
                    zTotal = Math.sqrt((Math.pow(zReal + imReal, 2) + Math.pow(zImg + imImg, 2)));
                    zTotal_ph = Math.toDegrees(Math.atan( (zImg + imImg) / (zReal + imReal) ));
        }
        double impedences[] = {zTotal, zTotal_ph, z_ph, zReal, zImg, imReal, imImg, zTL, zTL_ph};
        return impedences;
    }


    static void calculateCurrent(int src, int load, data Voltage, data Current, 
            double[] impedences) {
        double zTL = impedences[0];
        double zTL_ph = impedences[1];
        double z_ph = impedences[2];
        
        if (src == 1 && load == 1) {    //Wye-wye connection
            Current.Line = Voltage.Load / zTL;
            Current.LinePhase = Voltage.LoadPhase - zTL_ph;
            Current.Load = Current.Line;
            Current.LoadPhase = Current.LinePhase;
        }
        
        if (src == 1 && load == 2) {    //Wye-delta connection
            Current.Line = Voltage.Load / zTL;
            Current.LinePhase = Voltage.LoadPhase - zTL_ph;
            Current.Load = Current.Line / Math.sqrt(3);
            Current.LoadPhase = Current.LinePhase + 30;
        }        
        
        if (src == 2 && load == 2) {    //delta-delta connection
            Current.Line = Voltage.Line / Math.sqrt(3) / zTL;
            Current.LinePhase = Voltage.LoadPhase - 30 - zTL_ph;
            Current.Load = Current.Line / Math.sqrt(3);
            Current.LoadPhase = Current.LinePhase + 30;        
        }
        
        if (src == 2 && load == 1) {    //delta-wye connection
            Current.Line = Voltage.Line / Math.sqrt(3) / zTL;
            Current.LinePhase = Voltage.LoadPhase - 30 - zTL_ph;
            Current.Load = Current.Line;
            Current.LoadPhase = Current.LinePhase;        
        }
        
        printVoltage(Voltage); //print phase voltages and line voltages
        printCurrent(Current); //print phase currents and line currents
        printVoltagedrop(Current, impedences);
       
        //Power calculations
        double[] res = calculatePower(z_ph, Voltage, Current, impedences);

        printPower(res, z_ph); //print power calculations
    }
    
    static double[] calculatePower(double theta, data V, data C, double[] impedences) {
        theta = Math.toRadians(theta);
        double zReal = impedences[3];
        double zImg = impedences[4];
        double imReal = impedences[5];
        double imImg = impedences[6];
        double Pf = Math.cos(theta);
        double P = 3 * C.Load * C.Load * zReal;
        double Q = 3 * C.Load * C.Load * zImg;
        double P_losses = 3 * C.Line * C.Line * imReal;
        double Q_losses = 3 * C.Line * C.Line * imImg;
        double S_losses = Math.sqrt(Math.pow(P_losses, 2)+Math.pow(Q_losses, 2));
        double S = Math.sqrt(Math.pow(Q, 2)+Math.pow(P, 2));

        double res[] = {Pf, P, Q, S, P_losses, Q_losses, S_losses};
        return res;
    }

    static void printVoltage(data Voltage) {
        System.out.print("\n" + "At the source end: \n"
                + "V_phase_1 = " + Voltage.Load + " V     " + "Phase : " + Voltage.LoadPhase + "°\n"
                + "V_phase_2 = " + Voltage.Load + " V     " + "Phase : " + (Voltage.LoadPhase - 120) + "°\n"
                + "V_phase_3 = " + Voltage.Load + " V     " + "Phase : " + (Voltage.LoadPhase + 120) + "°\n");
        System.out.print("\n"
                + "V_line_1 = " + Voltage.Line + " V     " + "Phase : " + Voltage.LinePhase + "°\n"
                + "V_line_2 = " + Voltage.Line + " V     " + "Phase : " + (Voltage.LinePhase - 120) + "°\n"
                + "V_line_3 = " + Voltage.Line + " V     " + "Phase : " + (Voltage.LinePhase + 120) + "°\n");
    }
    
    static void printVoltagedrop(data Current, double[] impedences) {
        double zL = Math.sqrt((impedences[3]*impedences[3])+(impedences[4]*impedences[4]));
        double phi = (Current.LoadPhase+impedences[2]);
        double V_L = Current.Load*zL;
            System.out.print("\nVoltage drop across each load impedance\n"
                + "V_load_1 = " + V_L + " V     " + "Phase : " + (phi) + "°\n"
                + "V_load_2 = " + V_L + " V     " + "Phase : " + (phi-120) + "°\n"
                + "V_load_3 = " + V_L + " V     " + "Phase : " + (phi+120) + "°\n");
        double zTL = impedences[7];
        double psi = (Current.LinePhase+impedences[8]);
        double V_TL = Current.Line * zTL;
        System.out.print("\nVoltage drop across each line impedance\n"
                + "V_TL_1 = " + V_TL + " V     " + "Phase : " + (psi) + "°\n"
                + "V_TL_2 = " + V_TL + " V     " + "Phase : " + (psi - 120) + "°\n"
                + "V_TL_3 = " + V_TL + " V     " + "Phase : " + (psi + 120) + "°\n");
    }

    static void printCurrent(data Current) {
        System.out.print("\n"
                + "I_phase_1 = " + Current.Load + " A     " + "Phase : " + Current.LoadPhase + "°\n"
                + "I_phase_2 = " + Current.Load + " A     " + "Phase : " + (Current.LoadPhase - 120) + "°\n"
                + "I_phase_3 = " + Current.Load + " A     " + "Phase : " + (Current.LoadPhase + 120) + "°\n");
        System.out.print("\n"
                + "I_line_1 = " + Current.Line + " A     " + "Phase : " + Current.LinePhase + "°\n"
                + "I_line_2 = " + Current.Line + " A     " + "Phase : " + (Current.LinePhase - 120) + "°\n"
                + "I_line_3 = " + Current.Line + " A     " + "Phase : " + (Current.LinePhase + 120) + "°\n");
    }

    static void printPower(double[] res, double z_ph) {
        System.out.println("\n"
                + "P_load = " + res[1] + " W\n"
                + "Q_load = " + res[2] + " VAR\n"
                + "S_load = " + res[1] + " + " + res[2] + "j VA\n"
                + "P_losses = " + res[4] + " W\n"
                + "Q_losses = " + res[5] + " VAR\n"
                + "S_losses = " + res[4] + " + " + res[5] + "j VA\n"
                + "S_total = " + (res[1]+res[4]) + " + " + (res[2]+res[5]) + "j VA\n"
                + "Pf = " + res[0] + ((z_ph == 0) ? "" : "") + ((z_ph > 0) ? " lagging" : "") + ((z_ph < 0) ? " leading" : "") + "\n"
                + "Pf angle = " + Math.toDegrees(Math.acos(res[0])) +  "°\n");
    }
}
