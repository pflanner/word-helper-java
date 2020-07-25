import sys


def insert_word_into_file_contents(word_to_add, infile='', outfile=None):
    line_to_add = word_to_add + '\n'

    if outfile is None:
        outfile = infile

    with open(infile, mode='r') as f:
        contents = f.readlines()

    prev = ''
    inserted = False
    for i, line in enumerate(contents):
        if line == line_to_add:
            print('{} was already in the dictionary'.format(word_to_add))
            exit(0)

        if prev < line_to_add < line:
            contents.insert(i, line_to_add)
            inserted = True
            break

        prev = line

    if not inserted:
        contents.append(line_to_add)

    with open(outfile, mode='w') as f:
        f.writelines(contents)
        print('inserted {} into {}'.format(word_to_add, outfile))


def main():
    if len(sys.argv) < 2:
        print('word to add must be supplied as a parameter')
        exit(1)

    word_to_add = sys.argv[1]
    files = {
        '../resources/words.txt': 100,
        '../resources/words-standard.txt': 15,
        '../resources/words-easy.txt': 11,
    }

    for filename, max_word_length in files.items():
        if len(word_to_add) <= max_word_length:
            insert_word_into_file_contents(word_to_add, filename)
        else:
            print('word to long for {}, skipping'.format(filename))


if __name__ == '__main__':
    main()
