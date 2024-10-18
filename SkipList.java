// Dylan Benson, dy087044
// COP 3502, Spring 2023
// Assignment 4: Skip List

import java.util.*;

class Node <T extends Comparable<T>>
{
    private T data;
    private ArrayList<Node<T>> next;

    // Used to create head node with no data
    Node(int height)
    {
        this.next = new ArrayList<Node<T>>(height);
        this.data = null;

        for (int i = 0; i < height; i++)
        {
            next.add(null);
        }
    }

    // Used to create a node that holds data
    Node(T data, int height)
    {
        this.data = data;
        this.next = new ArrayList<Node<T>>(height);

        for (int i = 0; i < height; i++)
        {
            next.add(null);
        }
    }

    // Returns value of node
    public T value()
    {
        return this.data;
    }

    // Returns height of the node
    public int height()
    {
        return next.size();
    }

    // Returns next node at given level
    public Node<T> next(int level)
    {
        if (level < 0 || level > height() - 1)
            return null;
        
            return next.get(level);
    }

    // Sets the next node to the given node at the given level
    public void setNext(int level, Node<T> node)
    {
        next.set(level, node);
    }

    // Adds a level to the next list
    public void grow()
    {
        next.add(null);
    }

    // Coinflips growth of a previously maximally tall node
    public boolean maybeGrow()
    {
        int coinflip = (int) (Math.random() * 2);

        if (coinflip == 1)
        {
            grow();
            return true;
        }

        return false;
    }

    // Removes levels until at the desired height
    public void trim(int height)
    {
        for (int i = height() - 1; i >= height; i--)
        {
            next.remove(i);
        }
    }
}

public class SkipList <T extends Comparable<T>>
{
    private Node<T> head;
    private int size;

    SkipList()
    {
        this.head = new Node<T>(1);
        this.size = 0;
    }

    SkipList(int height)
    {
        if (height < 1) height = 1;
        this.head = new Node<T>(height);
        this.size = 0;
    }

    // Returns size of the list
    public int size()
    {
        return this.size;
    }

    // Returns current height of the list
    public int height()
    {
        return head.height();
    }

    // Returns the head of the list
    public Node<T> head()
    {
        return head;
    }

    // Inserts data with a random height
    public void insert(T data)
    {
        insert(data, generateRandomHeight(getMaxHeight(size() + 1)));
    }

    // Inserts data with a given height
    public void insert(T data, int height)
    {
        this.size += 1;

        // Checks to see if list needs to be grown
        if (getMaxHeight(size) > height())
            growSkipList();
        
        Node<T> current = head();
        Node<T> newNode = new Node<T>(data, height);

        // Starts at top of head node, finds correct position, and sets all next pointers
        for (int i = height() - 1; i >= 0; i--)
        {
            while (current.next(i) != null && current.next(i).value().compareTo(data) < 0)
            {
                current = current.next(i);
            }

            if (i <= height - 1)
            {
                    newNode.setNext(i, current.next(i));
                    current.setNext(i, newNode);
            }
        }
    }

    // Deletes data from the list and updates next pointers
    public void delete(T data)
    {
        Node<T> current = head();
        ArrayList<Node<T>> references = new ArrayList<>(head().height());

        // Initializes references array
        for (int i = 0; i < head().height(); i++)
            references.add(null);

        // Finds correct node and deletes it from the list
        // Gets previous pointers using reference array
        for (int i = height() - 1; i >= 0; i--)
        {
            while (current.next(i) != null && current.next(i).value().compareTo(data) < 0)
            {
                current = current.next(i);
            }
            
            if (current.next(i) != null && current.next(i).value().compareTo(data) == 0)
            {
                references.set(i, current);
            }
        }

        current = current.next(0);
        
        // If the data doesn't exist in the list, return
        if (current == null || current.value().compareTo(data) != 0)
        {
            return;
        }

        // Updates old pointers using references array
        for (int i = 0; i < current.height(); i++)
        {
            if(references.get(i).next(i).value().compareTo(data) != 0)
                break;
            references.get(i).setNext(i, current.next(i));
        }

        size -= 1;

        // Checks to see if the list needs to be trimmed
        if (getMaxHeight(size) < height())
            trimSkipList();
    }

    // Checks if given data exists in the list
    public boolean contains(T data)
    {
        Node<T> current = head();

        for (int i = height() - 1; i >= 0; i--)
        {
            while (current.next(i) != null && current.next(i).value().compareTo(data) < 0)
            {
                current = current.next(i);
            }

            if (current.next(i) != null && current.next(i).value().compareTo(data) == 0)
                return true;
        }

        return false;
    }

    // Similar to contains, but returns first node containing given data
    public Node<T> get(T data)
    {
        Node<T> current = head();

        for (int i = height() - 1; i >= 0; i--)
        {
            while (current.next(i) != null && current.next(i).value().compareTo(data) < 0)
            {
                current = current.next(i);
            }

            if (current.next(i) != null && current.next(i).value().compareTo(data) == 0)
                return current.next(i);
        }

        return null;
    }

    // Calculates max height based on current size of list
    private int getMaxHeight(int n)
    {
        if (n <= 2) return 1;

        return (int) Math.ceil(Math.log(n) / Math.log(2));
    }

    // Generates a random height by "flipping coins" until a "loss" occurs
    private static int generateRandomHeight(int maxHeight)
    {
        int counter = 1;
        int coinflip = (int) (Math.random() * 2);

        while (coinflip == 1 && counter < maxHeight)
        {
            counter++;
            coinflip = (int) (Math.random() * 2);
        }

        return counter;
    }

    // Grows list
    // Grows head node, 50% chance to grow all other maximally tall nodes
    private void growSkipList()
    {
        int prevHeight = head().height() - 1;
        head().grow();
        int newHeight = head().height() - 1;

        Node<T> current = head().next(prevHeight);
        Node<T> previous = head();

        while (current != null)
        {
            if (current.maybeGrow())
            {
                previous.setNext(newHeight, current);
                previous = current;
            }
            current = current.next(prevHeight);
        }

    }

    // Trims list
    private void trimSkipList()
    {
        int maxHeight = getMaxHeight(size);
        Node<T> tempNode = head();
        Node<T> current = head();

        do
        {
            tempNode = current.next(maxHeight);
            current.trim(maxHeight);
            current = tempNode;
        } while (current != null);

    }

    public static double difficultyRating()
    {
        return 4.5;
    }

    public static double hoursSpent()
    {
        return 17.0;
    }

}