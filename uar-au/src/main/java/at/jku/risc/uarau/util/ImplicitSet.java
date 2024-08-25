package at.jku.risc.uarau.util;

import java.util.*;

public class ImplicitSet<E> implements Set<E>, Queue<E> {
    private final E[] elements;
    
    private Integer hash = null;
    
    @SuppressWarnings("unchecked")
    public ImplicitSet(Collection<E> collection) {
        if (collection instanceof ImplicitSet) {
            ImplicitSet<E> implicitSet = (ImplicitSet<E>) collection;
            elements = implicitSet.elements;
            hash = implicitSet.hash;
            return;
        }
        
        elements = (E[]) new Object[collection.size()];
        int i = 0;
        for (E e : collection) {
            assert e != null;
            elements[i++] = e;
        }
        assert _Data.unique(this);
    }
    
    @SafeVarargs
    public ImplicitSet(E... e) {
        this(Arrays.asList(e));
    }
    
    @Override
    public int size() {
        return elements.length;
    }
    
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    
    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        for (E e : this) {
            if (o.equals(e)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean containsAll(Collection c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public Iterator<E> iterator() {
        return new ISetIterator();
    }
    
    // *** modifications ***
    
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
        if (size() == 0) {
            throw new IllegalArgumentException();
        }
        return elements[0];
    }
    
    @Override
    public E peek() {
        return size() == 0 ? null : elements[0];
    }
    
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public E[] toArray() {
        return Arrays.copyOf(elements, elements.length);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size()) {
            a = (T[]) new Object[size()];
        }
        try {
            for (E e : this) {
                T t = (T) e;
            }
        } catch (ClassCastException e) {
            throw new ArrayStoreException();
        }
        int i = 0;
        for (E e : this) {
            a[i++] = (T) e;
        }
        if (a.length > size()) {
            a[size()] = null;
        }
        return a;
    }
    
    private class ISetIterator implements Iterator<E> {
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
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ImplicitSet)) {
            return false;
        }
        ImplicitSet<?> other = (ImplicitSet<?>) obj;
        if (size() != other.size() || hashCode() != other.hashCode()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (!(elements[i].equals(other.elements[i]))) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        if (hash == null) {
            hash = 0;
            for (E e : this) {
                hash = hash * 31 + e.hashCode();
            }
        }
        return hash;
    }
}
