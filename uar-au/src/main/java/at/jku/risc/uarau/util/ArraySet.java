package at.jku.risc.uarau.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Compact representation of immutable sets, optimized for small- to medium-sized sets.
 * <br>
 * Doesn't allow null elements.
 */
public class ArraySet<E> implements Set<E> {
    private final E[] elements;
    private Integer hash = null;
    
    @SuppressWarnings("unchecked")
    public ArraySet(Collection<E> collection, boolean knownAsUnique) {
        if (collection instanceof ArraySet) {
            ArraySet<E> arraySet = (ArraySet<E>) collection;
            elements = arraySet.elements;
            hash = arraySet.hash;
        } else if (knownAsUnique || collection instanceof Set) {
            elements = (E[]) collection.toArray();
        } else {
            elements = (E[]) collection.stream().distinct().toArray();
        }
    }
    
    public ArraySet(Collection<E> collection) {
        this(collection, false);
    }
    
    public static <E> ArraySet<E> singleton(E element) {
        return new ArraySet<>(element);
    }
    
    /**
     * access with {@linkplain ArraySet#singleton(Object)}
     */
    @SuppressWarnings("unchecked")
    private ArraySet(E element) {
        elements = (E[]) new Object[]{element};
    }
    
    public <M> ArraySet<M> map(Function<E, M> mapFunction) {
        List<M> mapped = this.stream().map(mapFunction).distinct().collect(Collectors.toList());
        return new ArraySet<>(mapped, true);
    }
    
    public ArraySet<E> filter(Predicate<E> filterPredicate) {
        List<E> list = this.stream().filter(filterPredicate).collect(Collectors.toList());
        if (list.size() == this.size()) {
            return new ArraySet<>(this);
        }
        return new ArraySet<>(list, true);
    }
    
    public static <E> ArraySet<E> merged(ArraySet<E> a, ArraySet<E> b) {
        List<E> merged = Stream.concat(a.stream(), b.stream()).unordered().distinct().collect(Collectors.toList());
        return new ArraySet<>(merged, true);
    }
    
    // *** overrides ***
    
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
        assert o != null;
        for (E element : this) {
            if (o.equals(element)) {
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
            for (E element : this) {
                T ignored = (T) element;
            }
        } catch (ClassCastException e) {
            throw new ArrayStoreException();
        }
        int i = 0;
        for (E element : this) {
            a[i++] = (T) element;
        }
        if (a.length > size()) {
            a[size()] = null;
        }
        return a;
    }
    
    // *** equals/hashCode ***
    
    /**
     * <b>note:</b>
     * <br>
     * equals + hashCode consider insert order, since we only use them in {@code ProximityMap.proximatesMemory},
     * where we will usually only see a few insert orders per set of elements.
     * <br>
     * This made the (more correct) O(n^2) {@linkplain ArraySet#containsAll(Collection)} check noticeably slower on my test input.
     */
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ArraySet)) {
            return false;
        }
        ArraySet<?> other = (ArraySet<?>) obj;
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
            for (E element : this) {
                hash = hash * 31 + element.hashCode();
            }
        }
        return hash;
    }
    
    // *** iterator ***
    
    @Override
    public Iterator<E> iterator() {
        return new ImplicitSetIterator();
    }
    
    private class ImplicitSetIterator implements Iterator<E> {
        
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
    
    // *** modifications (unsupported) ***
    
    @Override
    @Deprecated // unsupported
    public boolean add(E element) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    @Deprecated // unsupported
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    @Deprecated // unsupported
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    @Deprecated // unsupported
    public void clear() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    @Deprecated // unsupported
    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    @Deprecated // unsupported
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
    }
}
