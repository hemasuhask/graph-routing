import java.io.*;
import java.util.*;

/**
 * Models a weighted graph of latitude-longitude points
 * and supports various distance and routing operations.
 * To do: Add your name(s) as additional authors
 * 
 * @author Brandon Fain
 * @author Owen Astrachan modified in Fall 2023
 *
 */
public class GraphProcessor {
    /**
     * Creates and initializes a graph from a source data
     * file in the .graph format. Should be called
     * before any other methods work.
     * 
     * @param file a FileInputStream of the .graph file
     * @throws Exception if file not found or error reading
     */

    private Map<Point, Set<Point>> graph;
    private int numVertices;
    private int numEdges;
    private Point[] vertices;
    private String[] vertexNames;

    public GraphProcessor() {
        graph = new HashMap<>();
        numVertices = 0;
        numEdges = 0;
    }

    /**
     * Creates and initializes a graph from a source data
     * file in the .graph format. Should be called
     * before any other methods work.
     * 
     * @param file a FileInputStream of the .graph file
     * @throws IOException if file not found or error reading
     */

    public void initialize(FileInputStream file) throws IOException {
        Scanner scanner = new Scanner(file);

        String[] header = scanner.nextLine().split(" ");

        numVertices = Integer.parseInt(header[0]);
        numEdges = Integer.parseInt(header[1]);

        vertexNames = new String[numVertices];
        vertices = new Point[numVertices];

        for (int i = 0; i < numVertices; i++) {
            if (!scanner.hasNextLine()) {
                scanner.close();
                throw new IOException("Could not read .graph file");
            }
            String[] node = scanner.nextLine().split(" ");
            vertexNames[i] = node[0];

            Point vertex = new Point(Double.parseDouble(node[1]), Double.parseDouble(node[2]));

            graph.put(vertex, new HashSet<>());
            vertices[i] = vertex;
        }

        for (int i = 0; i < numEdges; i++) {
            if (!scanner.hasNextLine()) {
                scanner.close();
                throw new IOException("Could not read .graph file");
            }
            String[] edge = scanner.nextLine().split(" ");
            Point vertex1 = vertices[Integer.parseInt(edge[0])];
            Point vertex2 = vertices[Integer.parseInt(edge[1])];

            graph.get(vertex1).add(vertex2);
            graph.get(vertex2).add(vertex1);
        }

        scanner.close();
    }

    /**
     * NOT USED IN FALL 2023, no need to implement
     * 
     * @return list of all vertices in graph
     */

    public List<Point> getVertices() {
        return null;
    }

    /**
     * NOT USED IN FALL 2023, no need to implement
     * 
     * @return all edges in graph
     */
    public List<Point[]> getEdges() {
        return null;
    }

    /**
     * Searches for the point in the graph that is closest in
     * straight-line distance to the parameter point p
     * 
     * @param p is a point, not necessarily in the graph
     * @return The closest point in the graph to p
     */
    public Point nearestPoint(Point referencePoint) {
        Point closestPoint = null;
        double minimumDistance = Double.MAX_VALUE;
    
        for (Point currentPoint : vertices) {
            if (referencePoint != currentPoint) {
                double currentDistance = referencePoint.distance(currentPoint);
                if (currentDistance < minimumDistance) {
                    closestPoint = currentPoint;
                    minimumDistance = currentDistance;
                }
            }
        }
        return closestPoint;
    }

    /**
     * Calculates the total distance along the route, summing
     * the distance between the first and the second Points,
     * the second and the third, ..., the second to last and
     * the last. Distance returned in miles.
     * 
     * @param start Beginning point. May or may not be in the graph.
     * @param end   Destination point May or may not be in the graph.
     * @return The distance to get from start to end
     */
    public double routeDistance(List<Point> pathPoints) {
        double totalDistance = 0.0;
        int numberOfPoints = pathPoints.size();
    
        for (int index = 0; index < numberOfPoints - 1; index++) {
            Point currentPoint = pathPoints.get(index);
            Point nextPoint = pathPoints.get(index + 1);
            totalDistance += currentPoint.distance(nextPoint);
        }
    
        return totalDistance;
    }

    /**
     * Checks if input points are part of a connected component
     * in the graph, that is, can one get from one to the other
     * only traversing edges in the graph
     * 
     * @param p1 one point
     * @param p2 another point
     * @return true if and onlyu if p2 is reachable from p1 (and vice versa)
     */
    public boolean connected(Point point1, Point point2) {
        Queue<Point> pointsToVisit = new LinkedList<>();
        HashSet<Point> visitedPoints = new HashSet<>();
    
        pointsToVisit.add(point1);
    
        while (!pointsToVisit.isEmpty()) {
            Point currentPoint = pointsToVisit.poll();
            visitedPoints.add(currentPoint);
    
            for (Point adjacentPoint : graph.get(currentPoint)) {
                if (visitedPoints.contains(adjacentPoint)) {
                    continue;
                }
                if (adjacentPoint.equals(point2)) {
                    System.out.println("Found path.");
                    return true;
                }
                pointsToVisit.add(adjacentPoint);
            }
        }
        return false;
    }

    /**
     * Returns the shortest path, traversing the graph, that begins at start
     * and terminates at end, including start and end as the first and last
     * points in the returned list. If there is no such route, either because
     * start is not connected to end or because start equals end, throws an
     * exception.
     * 
     * @param start Beginning point.
     * @param end   Destination point.
     * @return The shortest path [start, ..., end].
     * @throws IllegalArgumentException if there is no such route,
     *                                  either because start is not connected to end
     *                                  or because start equals end.
     */
    public List<Point> route(Point startPoint, Point endPoint) throws IllegalArgumentException {
        if (startPoint.equals(endPoint)) {
            throw new IllegalArgumentException();
        }
        Map<Point, Integer> pointIndices = new HashMap<>();
    
        double[] distances = new double[vertices.length];
        Map<Point, ArrayList<Point>> predecessorMap = new HashMap<>();
    
        for (int i = 0; i < vertices.length; i++) {
            if (vertices[i].equals(startPoint)) {
                distances[i] = 0;
            } else {
                distances[i] = Double.MAX_VALUE;
            }
            pointIndices.put(vertices[i], i);
        }
    
        PriorityQueue<Point> priorityQueue = new PriorityQueue<>(
                (point1, point2) -> (int) (distances[pointIndices.get(point1)] - distances[pointIndices.get(point2)]));
        HashSet<Point> visitedPoints = new HashSet<>();
    
        priorityQueue.add(startPoint);
    
        Point currentPoint = null;
    
        while (!priorityQueue.isEmpty()) {
            currentPoint = priorityQueue.poll();
            visitedPoints.add(currentPoint);
    
            for (Point adjacentPoint : graph.get(currentPoint)) {
                if (!visitedPoints.contains(adjacentPoint)) {
                    predecessorMap.putIfAbsent(adjacentPoint, new ArrayList<>());
                    predecessorMap.get(adjacentPoint).add(currentPoint);
                    distances[pointIndices.get(adjacentPoint)] = distances[pointIndices.get(currentPoint)] + currentPoint.distance(adjacentPoint);
                    priorityQueue.add(adjacentPoint);
                }
            }
        }
    
        List<Point> pathPoints = new ArrayList<>();
    
        currentPoint = endPoint;
    
        while (currentPoint != startPoint) {
            pathPoints.add(0, currentPoint);
            ArrayList<Point> predecessors = predecessorMap.get(currentPoint);
    
            if (predecessors == null) {
                throw new IllegalArgumentException("No path between start and end");
            }
    
            Point closestPredecessor = predecessors.get(0);
            double minDistance = distances[pointIndices.get(predecessors.get(0))];
    
            for (Point predecessor : predecessors.subList(1, predecessors.size())) {
                if (distances[pointIndices.get(predecessor)] < minDistance) {
                    closestPredecessor = predecessor;
                    minDistance = distances[pointIndices.get(predecessor)];
                }
            }
            currentPoint = closestPredecessor;
        }
    
        pathPoints.add(0, startPoint);
    
        return pathPoints;
    }


}
