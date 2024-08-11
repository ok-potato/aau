package at.jku.risc.uarau.util;

import java.util.*;

public class UnmodifiableDeque<E> implements Deque<E> {
    
    private final E[] elements;
    
    @SuppressWarnings("unchecked")
    public UnmodifiableDeque(Collection<E> collection) {
        elements = (E[]) new Object[collection.size()];
        int i = 0;
        for (E e : collection) {
            elements[i++] = e;
        }
    }
    
    @SafeVarargs
    public UnmodifiableDeque(E... e) {
        elements = Arrays.copyOf(e, e.length);
    }
    
    @Override
    public void addFirst(E e) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void addLast(E e) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean offerFirst(E e) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean offerLast(E e) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public E removeFirst() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public E removeLast() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public E getFirst() {
        E e = peekFirst();
        if (e == null) {
            throw new NoSuchElementException();
        }
        return e;
    }
    
    @Override
    public E getLast() {
        E e = peekLast();
        if (e == null) {
            throw new NoSuchElementException();
        }
        return e;
    }
    
    @Override
    public E peekFirst() {
        if (elements.length == 0) {
            return null;
        }
        return elements[0];
    }
    
    @Override
    public E peekLast() {
        if (elements.length == 0) {
            return null;
        }
        return elements[elements.length - 1];
    }
    
    @Override
    public boolean removeFirstOccurrence(Object o) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean removeLastOccurrence(Object o) {
        throw new UnsupportedOperationException();
    }
    
    // *** Queue methods ***
    
    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean offer(E e) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public E remove() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public E poll() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public E element() {
        return getFirst();
    }
    
    @Override
    public E peek() {
        return peekFirst();
    }
    
    // *** Stack methods ***
    
    @Override
    public void push(E e) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public E pop() {
        throw new UnsupportedOperationException();
    }
    
    // *** Collection methods ***
    
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean contains(Object o) {
        for (E e : elements) {
            if (o == null && e == null || o != null && o.equals(e)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int size() {
        return elements.length;
    }
    
    @Override
    public Iterator<E> iterator() {
        return new DeqIterator();
    }
    
    @Override
    public Iterator<E> descendingIterator() {
        return new DescDeqIterator();
    }
    
    // *** Collection methods ctd. ***
    
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    
    @Override
    public Object[] toArray() {
        return Arrays.copyOf(elements, elements.length);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < elements.length) {
            a = (T[]) new Object[elements.length];
        }
        try {
            for (E e : elements) {
                T t = (T) e;
            }
        } catch (ClassCastException e) {
            throw new ArrayStoreException();
        }
        for (int i = 0; i < elements.length; i++) {
            a[i] = (T) elements[i];
        }
        if (a.length > elements.length) {
            a[elements.length] = null;
        }
        return a;
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
    
    private class DeqIterator implements Iterator<E> {
        int cursor = 0;
        
        @Override
        public boolean hasNext() {
            return cursor < elements.length;
        }
        
        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return elements[cursor++];
        }
    }
    
    private class DescDeqIterator implements Iterator<E> {
        int cursor = elements.length - 1;
        
        @Override
        public boolean hasNext() {
            return cursor >= 0;
        }
        
        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return elements[cursor--];
        }
    }
}
