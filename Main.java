// Bibliotecas
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap;
import java.util.Collections;

// Classe para representar uma Cidade
class Cidade {
    private int id;
    private int index;
    private double x; 
    private double y;
    
    public int getId() { return id; }
    public int getIndex() { return index; }

    public double getXPos() { return x; }
    public double getYPos() { return y; }
    
    public Cidade(int i, int in, double lat, double lon){
        id = i;
        index = in;
        x = lat; 
        y = lon; 
    }
}

// Main
public class Main {
    public static ArrayList<Cidade> cidades;
    public static double[][] gr;

    // Força Bruta
    public static List<int[]> permutacao;
    // Programação Dinâmica
    public static List<Integer> pathDP;
    
    // Execução
    public static void main(String[] args) throws IOException,FileNotFoundException  {
        try {
            permutacao = new ArrayList<int[]>();
            cidades = new ArrayList<Cidade>();
            pathDP = new ArrayList<Integer>();
            
            // Leitura do arquivo de "input"
            File arquivoCidade= new File("input.txt");
            FileReader fr = new FileReader(arquivoCidade);
            BufferedReader br = new BufferedReader(fr);

            String line = br.readLine();
            line = br.readLine();

            int id = 1;
            int index = 0;
            while (line != null){
                String[] coordenadas = line.split(" ");

                Double lat = Double.parseDouble(coordenadas[0]);
                Double lon = Double.parseDouble(coordenadas[1]);

                cidades.add(new Cidade(id, index, lat, lon));
                line = br.readLine();
                id++;
                index++;
            }
            br.close();

            gr = grafoMatriz();
            Map.Entry<int[], Double> caminhoOtimo = new AbstractMap.SimpleEntry<int[], Double>(null, null);
            long startTime,endTime,totalTime;

            // Apagar arquivo de "output" caso exista
            File output = new File("output.txt");
            if (output.exists()){
                output.delete();
            }
            // Overview
            System.out.println("˜˜˜˜˜ Execução ˜˜˜˜˜\n");
            
            // Impressão da Força Bruta
            System.out.println("Calculando caminho utilizando Força Bruta...");
            startTime = System.currentTimeMillis();
            caminhoOtimo = forcaBruta(cidades, gr, permutacao);
            endTime = System.currentTimeMillis();
            totalTime = endTime-startTime;
            salvarArquivo(caminhoOtimo, totalTime,"Força Bruta");

            // Impressão da Programação Dinâmica
            System.out.println("Calculando caminho utilizando Programação Dinâmica...");
            startTime = System.currentTimeMillis();
            caminhoOtimo = progDinamica(cidades, gr, pathDP);
            endTime = System.currentTimeMillis();
            totalTime = endTime-startTime;
            salvarArquivo(caminhoOtimo,totalTime,"Programação Dinâmica");
    
            // Impressão do Algoritmo Guloso
            System.out.println("Calculando caminho utilizando Algoritmo Guloso...");
            startTime = System.currentTimeMillis();
            caminhoOtimo = algoritmoGuloso(cidades, gr); 
            endTime = System.currentTimeMillis();
            totalTime = endTime-startTime;
            salvarArquivo(caminhoOtimo,totalTime,"Algoritmo Guloso");
            
        } catch (Exception e) {
            System.out.println("Erro: "+e.getMessage()+"\n");
        }
        return;
    }

    // Arquivo - Salvar
    public static void salvarArquivo(Map.Entry<int[], Double> shortest, long time, String algoritmo) throws IOException {
        String otimo = "";
        String path = shortest.getValue().toString();
        path = path.substring(0, (path.indexOf(".")) + 3);
    
        for (int i=0;i<cidades.size(); i++){
            otimo += cidades.get(shortest.getKey()[i]).getId() + ", ";
        }
        otimo += cidades.get(shortest.getKey()[0]).getId();
                
        // Escrever resultados no arquivo de "output"
        File output = new File("output.txt");

        FileWriter fr = new FileWriter(output, true);
        BufferedWriter bw = new BufferedWriter(fr);
        bw.write(algoritmo+":\n "+"-- Cidades visitadas: "+cidades.size()+"\n -- Tempo total: "+ time +" ms\n -- Ordem das cidades visitadas: ["+otimo+"]\n -- Caminho mais curto encontrado: "+path+"\n\n");
        bw.close();
        System.out.println("-- Completo!\n");
    }

    // Grafo representado por uma "Matriz de Adjacência"
    public static double[][] grafoMatriz(){
        int vertices = cidades.size();
        double[][] matriz = new double[vertices][vertices];
        for (int from = 0; from < vertices; from++){
            for (int to = 0; to < vertices; to++){
                if (from == to){
                    matriz[from][to] = 0;
                } else {
                    matriz[from][to] = pontoDistancia(cidades.get(from).getXPos(),cidades.get(from).getYPos(),cidades.get(to).getXPos(),cidades.get(to).getYPos());
                }
            }
        }
        return matriz;
    }

    // MÉTODO 1: Força Bruta
    public static Map.Entry<int[], Double> forcaBruta(List<Cidade> cidades, double[][] gr, List<int[]> permutacao){
        int[] shortestCaminhoPermutacao = new int[cidades.size()];
        double caminhoOtimo = Double.MAX_VALUE;
        
        List<Integer> indexOfCitiesToPermute = new ArrayList<Integer>();
        cidades.forEach(city -> indexOfCitiesToPermute.add(city.getIndex()));
        
        permutacao(indexOfCitiesToPermute,0, permutacao);
        
        for (int i = 0; i < permutacao.size(); i++){
            double distanciaPermutacao = permutacao_distancia(permutacao.get(i), gr);
            if (distanciaPermutacao < caminhoOtimo){
                caminhoOtimo = distanciaPermutacao;
                shortestCaminhoPermutacao = permutacao.get(i);
            }
        }
        return new AbstractMap.SimpleEntry<int[], Double>(shortestCaminhoPermutacao, caminhoOtimo);
    }

    // MÉTODO 2: Programacao Dinâmica
    public static Map.Entry<int[], Double> progDinamica(List<Cidade> cidades, double[][] gr, List<Integer> pathDP){
        int size = cidades.size();
        int sizePow = (int) Math.pow(2, cidades.size());
        
        int[][] graphPath = new int[size][sizePow]; 
        double[][] graphDP = new double[size][sizePow];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < sizePow; j++) {
				graphPath[i][j] = -1;
                graphDP[i][j] = -1;
			}
		}

        for (int i = 0; i < size; i++) {
			graphDP[i][0] = gr[i][0];
		}

        pathDP.add(0);
        Double pathValue = Heuristica(0, sizePow - 2, gr, graphDP, graphPath, size, sizePow);
        GetPathByValue(0, sizePow - 2, graphPath, pathValue, sizePow, pathDP);
        int[] caminhoOtimo = pathDP.stream().mapToInt(Integer::intValue).toArray(); 
        
        return new AbstractMap.SimpleEntry<int[], Double>(caminhoOtimo, pathValue);
    }

    // MÉTODO 3: Algoritmo Guloso
    public static Map.Entry<int[], Double> algoritmoGuloso(List<Cidade> cidades, double[][] gr){
        List<Integer> path = new ArrayList<>();
        int size = cidades.size();
        
		double distanciaTotal = 0;
		int cidadeAtual = 0;
		path.add(cidadeAtual);
		int cidadeProxima = cidadeAtual;
		double distanciaMinima = Double.MAX_VALUE;

		while(path.size() < size) {
            for(int i = 0; i < size; i++) {
				if(i != cidadeAtual && !path.contains(i)) {
					if(gr[cidadeAtual][i] < distanciaMinima) {
						distanciaMinima = gr[cidadeAtual][i];
						cidadeProxima = i;
					}
				}
			}

			path.add(cidadeProxima);
			distanciaTotal += distanciaMinima;
			cidadeAtual = cidadeProxima;
			distanciaMinima = Double.MAX_VALUE;
		}

		distanciaTotal += gr[path.get(path.size() - 1)][0];
        int[] caminhoOtimo = path.stream().mapToInt(Integer::intValue).toArray(); 

        return new AbstractMap.SimpleEntry<int[], Double>(caminhoOtimo, distanciaTotal);
    }

    // Ponto de Distância
    public static double pontoDistancia(double x1, double y1, double x2, double y2){
        return Math.sqrt((Math.pow((x1 - x2), 2)) + (Math.pow((y1 - y2), 2)));
    }

    // Permutação
    public static void permutacao(List<Integer> ids, int k, List<int[]> permutacao){
        for(int i = k; i < ids.size(); i++){
            Collections.swap(ids, i, k);
            permutacao(ids, k+1, permutacao);
            Collections.swap(ids, k, i);
        }

        if (k == ids.size() -1 && ids.get(0) == 0) permutacao.add(ids.stream().mapToInt(i->i).toArray());
    }

    // Obter o caminho por valor
    public static void GetPathByValue(int startTime, int set, int[][] graphPath, double pathValue, int sizePow, List<Integer> pathDP){
        if(graphPath[startTime][set] == -1)
			return;
		int x = graphPath[startTime][set];
		int mascara = sizePow -1 - (int)Math.pow(2, x);
		int marcado = set & mascara;
		
		pathDP.add(x);
		GetPathByValue(x, marcado, graphPath, pathValue, sizePow, pathDP);
    }

    // Cálculo da "Heurística"
    public static Double Heuristica(int init, int set, double[][] gr, double[][] graphDP, int[][] graphPath, int size, int sizePow){
        int mascara, marcado;
        double resultado = -1, temp;
		
        if(graphDP[init][set] != -1)
			return graphDP[init][set];
		else {
            for (int i = 0; i < size; i++) {
                mascara = sizePow - 1 - (int) Math.pow(2, i);
                marcado = set & mascara;
                if(marcado != set) {
                    temp = (gr[init][i] + Heuristica(i, marcado, gr, graphDP, graphPath, size, sizePow));
                    if(resultado == -1 || resultado > temp) {
                        resultado = temp;
                        graphPath[init][set] = i;
                    }
                }
            }
            graphDP[init][set] = resultado;
            return resultado;
        }
    }

    // Permutação da distância
    public static double permutacao_distancia(int[] permutation, double[][] gr){
        int cidadeInicial = permutation[0];

        double distancia = 0;

        int fromID, toID;
        for (int i = 0; i < permutation.length; i++){
            fromID = permutation[i];

            if (i == permutation.length - 1){
                toID = cidadeInicial;
            } else {
                toID = permutation[i + 1];
            }
            distancia += gr[fromID][toID];
        }
        return distancia;
    }
}